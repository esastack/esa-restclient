/*
 * Copyright 2020 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package esa.httpclient.core.netty;

import esa.commons.Checks;
import esa.commons.ExceptionUtils;
import esa.commons.Platforms;
import esa.commons.annotation.Internal;
import esa.commons.concurrent.ThreadPools;
import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpMethod;
import esa.commons.http.HttpVersion;
import esa.commons.reflect.BeanUtils;
import esa.commons.spi.SpiLoader;
import esa.httpclient.core.CompositeRequest;
import esa.httpclient.core.Context;
import esa.httpclient.core.Handle;
import esa.httpclient.core.Handler;
import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpRequestFacade;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.IdentityFactory;
import esa.httpclient.core.Listener;
import esa.httpclient.core.ListenerProxy;
import esa.httpclient.core.ModifiableClient;
import esa.httpclient.core.config.CallbackThreadPoolOptions;
import esa.httpclient.core.config.ChannelPoolOptions;
import esa.httpclient.core.config.Decompression;
import esa.httpclient.core.config.SslOptions;
import esa.httpclient.core.exec.ExecContext;
import esa.httpclient.core.exec.HttpTransceiver;
import esa.httpclient.core.exec.RequestExecutor;
import esa.httpclient.core.exec.RequestExecutorImpl;
import esa.httpclient.core.metrics.CallbackExecutorMetric;
import esa.httpclient.core.metrics.ConnectionPoolMetric;
import esa.httpclient.core.metrics.ConnectionPoolMetricProvider;
import esa.httpclient.core.metrics.IoThreadGroupMetric;
import esa.httpclient.core.metrics.IoThreadMetric;
import esa.httpclient.core.spi.SslEngineFactory;
import esa.httpclient.core.util.Futures;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.SingleThreadEventLoop;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.internal.SystemPropertyUtil;

import javax.net.ssl.SSLException;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static esa.httpclient.core.netty.ChannelPoolFactory.PREFER_NATIVE;

@Internal
public class NettyHttpClient implements HttpClient, ModifiableClient<NettyHttpClient> {

    private static final String IOTHREADS_KYE = "esa.httpclient.ioThreads";
    private static final int IOTHREADS = SystemPropertyUtil.getInt(IOTHREADS_KYE,
            Math.min(Platforms.cpuNum() << 1, 16));

    private static final String CLOSE_CONNECTION_POOL_DELAY_SECONDS_KEY =
            "esa.httpclient.closeConnectionPoolDelaySeconds";
    private static final long CLOSE_CHANNEL_POOL_DELAY_SECONDS = SystemPropertyUtil
            .getLong(CLOSE_CONNECTION_POOL_DELAY_SECONDS_KEY, 120L);

    private static final String IOTHREADS_GRACEFULLY_SHUTDOWN_QUIET_PERIOD_KEY =
            "esa.httpclient.ioThreadsGracefullyShutdownQuietPeriod";
    private static final long IOTHREADS_GRACEFULLY_SHUTDOWN_QUIET_PERIOD =
            SystemPropertyUtil.getLong(IOTHREADS_GRACEFULLY_SHUTDOWN_QUIET_PERIOD_KEY, 2L);

    private static final String IOTHREADS_GRACEFULLY_SHUTDOWN_TIMEOUT_SECONDS_KEY =
            "esa.httpclient.ioThreadsGracefullyShutdownTimeoutSeconds";
    private static final long IOTHREADS_GRACEFULLY_SHUTDOWN_TIMEOUT_SECONDS =
            SystemPropertyUtil.getLong(IOTHREADS_GRACEFULLY_SHUTDOWN_TIMEOUT_SECONDS_KEY, 15L);

    private static final String IDENTITY_PREFIX = "ESAHttpClient-";
    private static final AtomicInteger IDENTITY = new AtomicInteger();
    private static final AtomicInteger ACTIVE_CLIENTS = new AtomicInteger();

    /**
     * Shared callback executor
     */
    private static final IdentityFactory.Identified<ThreadPoolExecutor> SHARED_CALLBACK_EXECUTOR =
            IdentityFactoryProvider.callbackExecutorIdentityFactory()
                    .generate(newCallbackExecutor(CallbackThreadPoolOptions.ofDefault()));

    /**
     * Shared io threads pool
     */
    private static final IdentityFactory.Identified<EventLoopGroup> SHARED_IO_THREADS = IdentityFactoryProvider
            .ioThreadsIdentityFactory().generate(sharedIoThreads());

    private static final ScheduledExecutorService CLOSE_CONNECTION_POOL_SCHEDULER =
            new ScheduledThreadPoolExecutor(1,
                    new ThreadFactoryImpl("ESAHttpClient-CloseConnectionPool-Scheduler", true),
                    (r, executor) -> LoggerUtils.logger().error(
                            "ESAHttpClient-CloseConnectionPool-Scheduler-Pool has full," +
                                    " a task has been rejected"));

    protected final HttpClientBuilder builder;

    private final CachedChannelPools channelPools;
    private final IdentityFactory.Identified<EventLoopGroup> ioThreads;
    private final IdentityFactory.Identified<ThreadPoolExecutor> callbackExecutor;
    private final String id;

    private volatile RequestExecutor executor;
    private final ChannelPoolFactory channelPoolFactory;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public NettyHttpClient(HttpClientBuilder builder, CachedChannelPools channelPools) {
        this(builder, channelPools, SHARED_IO_THREADS, SHARED_CALLBACK_EXECUTOR);
    }

    private NettyHttpClient(HttpClientBuilder builder,
                            CachedChannelPools channelPools,
                            IdentityFactory.Identified<EventLoopGroup> ioThreads,
                            IdentityFactory.Identified<ThreadPoolExecutor> callbackExecutor) {
        Checks.checkNotNull(builder, "builder");
        Checks.checkNotNull(ioThreads, "ioThreads");
        Checks.checkNotNull(channelPools, "channelPools");
        this.builder = builder;
        this.callbackExecutor = callbackExecutor;
        this.ioThreads = ioThreads;
        this.channelPools = channelPools;
        this.id = IDENTITY_PREFIX + IDENTITY.incrementAndGet();
        this.channelPoolFactory = new ChannelPoolFactory(loadSslEngineFactory(builder.sslOptions()));
        this.executor = build(ioThreads.origin(), channelPools, buildOptions(builder));
        ACTIVE_CLIENTS.incrementAndGet();
    }

    @Override
    public HttpRequestFacade get(String uri) {
        return newRequestFacade(HttpMethod.GET, uri);
    }

    @Override
    public HttpRequestFacade head(String uri) {
        return newRequestFacade(HttpMethod.HEAD, uri);
    }

    @Override
    public HttpRequestFacade options(String uri) {
        return newRequestFacade(HttpMethod.OPTIONS, uri);
    }

    @Override
    public HttpRequestFacade trace(String uri) {
        return newRequestFacade(HttpMethod.TRACE, uri);
    }

    @Override
    public HttpRequestFacade connect(String uri) {
        return newRequestFacade(HttpMethod.CONNECT, uri);
    }

    @Override
    public HttpRequestFacade post(String uri) {
        return newRequestFacade(HttpMethod.POST, uri);
    }

    @Override
    public HttpRequestFacade delete(String uri) {
        return newRequestFacade(HttpMethod.DELETE, uri);
    }

    @Override
    public HttpRequestFacade put(String uri) {
        return newRequestFacade(HttpMethod.PUT, uri);
    }

    @Override
    public HttpRequestFacade patch(String uri) {
        return newRequestFacade(HttpMethod.PATCH, uri);
    }

    /**
     * Executes the given {@link HttpRequest} and obtains the {@link HttpResponse}. If both {@code handle}
     * and {@code handler} are null, the default {@link DefaultHandle} will be used to aggregate the inbound
     * message to a {@link HttpResponse}.
     *
     * Be aware that, if the {@link CompletableFuture} is returned, which means that we will release
     * the {@link HttpRequest#buffer()} automatically even if it's an exceptionally {@code future}.
     * Besides, you should manage the {@link HttpRequest#buffer()} by yourself. eg:
     * <pre>
     *     final Buffer buffer = xxx;
     *     try {
     *         client.execute();
     *     } catch (Throwable th) {
     *         buffer.getByteBuf().release();
     *     }
     * </pre>
     *
     * @param request request
     * @param ctx     ctx
     * @param handle  handle, which may be null
     * @param handler handler, which may be null
     * @return response
     */
    public CompletableFuture<HttpResponse> execute(HttpRequest request,
                                                   Context ctx,
                                                   Consumer<Handle> handle,
                                                   Handler handler) {
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(ctx, "ctx");
        final Listener listener = ListenerProxy.DEFAULT;

        addAcceptEncodingIfAbsent(request);

        CompletableFuture<HttpResponse> response = executor.execute(request,
                new ExecContext(ctx, listener, handle, handler));
        if (request.buffer() != null) {
            response = response.whenComplete((rsp, th) -> Utils.tryRelease(request.buffer().getByteBuf()));
        }

        if (callbackExecutor.origin() == null) {
            return response;
        } else {
            // Note that: only if callback executor exists and the response
            // of original execution completes normally, we switch the original
            // response to continue execute in callback executor.
            return response.thenComposeAsync(Futures::completed, callbackExecutor.origin());
        }
    }

    @Override
    public ConnectionPoolMetricProvider connectionPoolMetric() {
        return channelPools;
    }

    @Override
    public IoThreadGroupMetric ioThreadsMetric() {
        if (ioThreads.origin() instanceof MultithreadEventLoopGroup) {
            return new IoThreadGroupMetricImpl((MultithreadEventLoopGroup) ioThreads.origin(), ioThreads.id());
        } else {
            return null;
        }
    }

    @Override
    public CallbackExecutorMetric callbackExecutorMetric() {
        if (callbackExecutor.origin() == null) {
            return null;
        }
        return new CallbackExecutorMetricImpl(callbackExecutor.origin(), callbackExecutor.id());
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            ACTIVE_CLIENTS.decrementAndGet();

            // Close channel pools and all according channels.
            channelPools.close();

            channelPoolFactory.sslEngineFactory.onDestroy();

            if (ACTIVE_CLIENTS.intValue() == 0) {
                closeGlobalGracefully();
            }
        }
    }

    private HttpRequestFacade newRequestFacade(HttpMethod method, String uri) {
        Checks.checkNotNull("method");
        Checks.checkNotEmptyArg(uri, "HttpRequest's uri must not be empty");
        return new CompositeRequest(builder, this,
                () -> new SegmentRequestImpl(builder, executor, method, uri),
                method, uri);
    }

    private static void closeGlobalGracefully() {
        // Shutdown Closing-ChannelPool-Scheduler
        try {
            CLOSE_CONNECTION_POOL_SCHEDULER.shutdown();
            CLOSE_CONNECTION_POOL_SCHEDULER.awaitTermination(5L, TimeUnit.SECONDS);
            List<Runnable> unfinishedTasks = CLOSE_CONNECTION_POOL_SCHEDULER.shutdownNow();
            String msg = "Closed NettyHttpClient-CloseConnectionPool-Scheduler-ThreadPool" +
                    " successfully, unfinished tasks: " + unfinishedTasks.size();

            if (unfinishedTasks.isEmpty()) {
                LoggerUtils.logger().info(msg);
            } else {
                LoggerUtils.logger().error(msg);
            }
        } catch (Throwable ex) {
            LoggerUtils.logger().error("Error while closing NettyHttpClient-CloseConnectionPool-" +
                    "Scheduler-ThreadPool", ex);
        }

        // Shutdown IO-Threads
        if (SHARED_IO_THREADS.origin() != null) {
            try {
                SHARED_IO_THREADS.origin().shutdownGracefully(IOTHREADS_GRACEFULLY_SHUTDOWN_QUIET_PERIOD,
                        IOTHREADS_GRACEFULLY_SHUTDOWN_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS);
            } catch (Throwable ex) {
                LoggerUtils.logger().error("Error while closing IO-Threads", ex);
            }
        }

        if (SHARED_CALLBACK_EXECUTOR.origin() != null) {
            try {
                SHARED_CALLBACK_EXECUTOR.origin().shutdown();

                final long gracefullyShutdownSeconds = CallbackThreadPoolOptions
                        .ofDefault().gracefullyShutdownSeconds();
                if (gracefullyShutdownSeconds > 0L) {
                    SHARED_CALLBACK_EXECUTOR.origin().awaitTermination(gracefullyShutdownSeconds,
                            TimeUnit.SECONDS);
                }
                List<Runnable> unfinishedTasks = SHARED_CALLBACK_EXECUTOR.origin().shutdownNow();
                String msg = "Closed Callback-Executor-ThreadPool successfully, unfinished tasks: " +
                        unfinishedTasks.size();
                if (unfinishedTasks.isEmpty()) {
                    LoggerUtils.logger().info(msg);
                } else {
                    LoggerUtils.logger().error(msg);
                }
            } catch (Throwable ex) {
                LoggerUtils.logger().error("Error while closing Callback-Executor-ThreadPool", ex);
            }
        }

        // Shutdown ReadTimeout-Timer
        HttpTransceiverImpl.closeTimer();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public synchronized NettyHttpClient applyChannelPoolOptions(ChannelPoolOptions options, boolean applyToExisted) {
        Checks.checkNotNull(options, "options");

        if (options.connectTimeout() == builder.connectTimeout()
                && options.readTimeout() == builder.readTimeout()
                && options.poolSize() == builder.connectionPoolSize()
                && options.waitingQueueLength() == builder.connectionPoolWaitingQueueLength()) {
            return this;
        }

        // NOTE: update the global channelPoolOptions hold in executor.
        this.executor = build(ioThreads.origin(), channelPools, options);

        if (!applyToExisted) {
            return this;
        }

        Map<SocketAddress, ConnectionPoolMetric> metrics = channelPools.all();
        metrics.forEach((addr, opt) -> {
            if (!options.equals(opt.options())) {
                applyChannelPoolOptions(channelPools.getIfPresent(addr), addr, options);
            }
        });

        return this;
    }

    @Override
    public synchronized NettyHttpClient applyChannelPoolOptions(Map<SocketAddress, ChannelPoolOptions> options) {
        if (options == null || options.isEmpty()) {
            return this;
        }

        for (Map.Entry<SocketAddress, ChannelPoolOptions> entry : options.entrySet()) {
            ChannelPool pre = channelPools.getIfPresent(entry.getKey());
            if (pre == null || pre.options.equals(entry.getValue())) {
                continue;
            }

            applyChannelPoolOptions(pre, entry.getKey(), entry.getValue());
        }

        return this;
    }

    private void applyChannelPoolOptions(ChannelPool old,
                                         SocketAddress address,
                                         ChannelPoolOptions options) {
        if (old == null) {
            return;
        }

        channelPools.put(address,
                channelPoolFactory.create(old.ssl, true, address, ioThreads.origin(), options, builder));
        CLOSE_CONNECTION_POOL_SCHEDULER.schedule(() -> CachedChannelPools.close(address, old, true),
                CLOSE_CHANNEL_POOL_DELAY_SECONDS,
                TimeUnit.SECONDS);
    }

    private ChannelPoolOptions buildOptions(HttpClientBuilder builder) {
        return ChannelPoolOptions.options()
                .connectTimeout(builder.connectTimeout())
                .readTimeout(builder.readTimeout())
                .poolSize(builder.connectionPoolSize())
                .waitingQueueLength(builder.connectionPoolWaitingQueueLength())
                .build();
    }

    /**
     * Build a {@link RequestExecutor} to execute given {@link HttpRequest} with given {@link Listener}.
     *
     * @param ioThreads            ioThreads
     * @param channelPools         channel pool map
     * @return executor
     */
    protected RequestExecutor build(EventLoopGroup ioThreads,
                                    CachedChannelPools channelPools,
                                    ChannelPoolOptions channelPoolOptions) {
        HttpTransceiver transceiver = new NettyTransceiver(ioThreads,
                channelPools,
                builder,
                channelPoolOptions,
                channelPoolFactory);

        return new RequestExecutorImpl(builder.unmodifiableInterceptors(), transceiver);
    }

    /**
     * Builds a {@link ThreadPoolExecutor} which to handle the callback logic with specified
     * {@link CallbackThreadPoolOptions}.
     *
     * @param options       options
     * @return executor
     */
    static ThreadPoolExecutor newCallbackExecutor(CallbackThreadPoolOptions options) {
        if (options == null) {
            return null;
        }

        final BlockingQueue<Runnable> workQueue = options.blockingQueueLength() > 0
                ? new LinkedBlockingQueue<>(options.blockingQueueLength())
                : new SynchronousQueue<>();

        return ThreadPools.builder().corePoolSize(options.coreSize())
                .maximumPoolSize(options.maxSize())
                .keepAliveTime(options.keepAliveSeconds())
                .workQueue(workQueue)
                .threadFactory(new ThreadFactoryImpl("HttpClient-Callback", true))
                .rejectPolicy((r, executor) -> LoggerUtils.logger().error("HttpClient-Callback-Thread-Pool" +
                        " has been full, a task was rejected"))
                .build();
    }

    private static EventLoopGroup sharedIoThreads() {
        if (PREFER_NATIVE && Epoll.isAvailable()) {
            return new EpollEventLoopGroup(IOTHREADS,
                    new ThreadFactoryImpl("NettyHttpClient-I/O", true));
        } else {
            return new NioEventLoopGroup(IOTHREADS,
                    new ThreadFactoryImpl("NettyHttpClient-I/O", true));
        }
    }

    /**
     * Adds accept-encoding if necessary
     *
     * @param request request
     */
    private void addAcceptEncodingIfAbsent(HttpRequest request) {
        if (request.headers().contains(HttpHeaderNames.ACCEPT_ENCODING)) {
            return;
        }

        if (builder.isUseDecompress()) {
            Decompression decompression;
            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING,
                    ((decompression = builder.decompression()) == null
                            ? Decompression.GZIP_DEFLATE.format() : decompression.format()));
        }
    }

    /**
     * Package visibility for unit test.
     *
     * @param sslOptions        sslOptions
     * @return                  factory
     */
    protected SslEngineFactory loadSslEngineFactory(SslOptions sslOptions) {
        List<SslEngineFactory> sslEngineFactories = SpiLoader.getAll(SslEngineFactory.class);
        if (!sslEngineFactories.isEmpty()) {
            return sslEngineFactories.get(0);
        }

        SslProvider provider = OpenSsl.isAvailable() ? SslProvider.OPENSSL : SslProvider.JDK;
        SslContextBuilder builder = SslContextBuilder.forClient();
        builder.sslProvider(provider);

        if (sslOptions != null && sslOptions.ciphers().length > 0) {
            builder.ciphers(Arrays.asList(sslOptions.ciphers()));
        }
        if (sslOptions != null && sslOptions.sessionTimeout() > 0L) {
            builder.sessionTimeout(sslOptions.sessionTimeout());
        }
        if (sslOptions != null && sslOptions.sessionCacheSize() > 0L) {
            builder.sessionCacheSize(sslOptions.sessionCacheSize());
        }
        if (sslOptions != null && sslOptions.useInsecureTrustManager()) {
            builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        }
        if (sslOptions != null && sslOptions.trustCertificates() != null) {
            builder.trustManager(sslOptions.trustCertificates());
        }

        // ALPN http2 upgrade negotiation
        if (HttpVersion.HTTP_2 == this.builder.version()) {
            builder.applicationProtocolConfig(new ApplicationProtocolConfig(
                    io.netty.handler.ssl.ApplicationProtocolConfig.Protocol.ALPN,
                    // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                    // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                    ApplicationProtocolNames.HTTP_2,
                    ApplicationProtocolNames.HTTP_1_1));
        }

        SslContext sslContext;
        try {
            sslContext = builder.build();
        } catch (SSLException ex) {
            throw ExceptionUtils.asRuntime(ex);
        }

        return new SslEngineFactoryImpl(sslContext);
    }

    static class CallbackExecutorMetricImpl implements CallbackExecutorMetric {

        private final ThreadPoolExecutor callbackExecutor;
        private final String id;

        CallbackExecutorMetricImpl(ThreadPoolExecutor callbackExecutor, String id) {
            this.callbackExecutor = callbackExecutor;
            this.id = id;
        }

        @Override
        public int coreSize() {
            return callbackExecutor.getCorePoolSize();
        }

        @Override
        public int maxSize() {
            return callbackExecutor.getMaximumPoolSize();
        }

        @Override
        public long keepAliveSeconds() {
            return callbackExecutor.getKeepAliveTime(TimeUnit.SECONDS);
        }

        @Override
        public int activeCount() {
            return callbackExecutor.getActiveCount();
        }

        @Override
        public int poolSize() {
            return callbackExecutor.getPoolSize();
        }

        @Override
        public int largestPoolSize() {
            return callbackExecutor.getLargestPoolSize();
        }

        @Override
        public long taskCount() {
            return callbackExecutor.getTaskCount();
        }

        @Override
        public int queueSize() {
            return callbackExecutor.getQueue().size();
        }

        @Override
        public long completedTaskCount() {
            return callbackExecutor.getCompletedTaskCount();
        }

        @Override
        public String executorId() {
            return id;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ",
                    CallbackExecutorMetricImpl.class.getSimpleName() + "[", "]")
                    .add("id='" + id + "'")
                    .add("coreSize=" + coreSize())
                    .add("maxSize=" + maxSize())
                    .add("keepAliveSeconds=" + keepAliveSeconds())
                    .add("activeCount=" + activeCount())
                    .add("poolSize=" + poolSize())
                    .add("largestPoolSize=" + largestPoolSize())
                    .add("taskCount=" + taskCount())
                    .add("queueSize=" + queueSize())
                    .add("completedTaskCount=" + completedTaskCount())
                    .toString();
        }
    }

    static class IoThreadGroupMetricImpl implements IoThreadGroupMetric {

        private final MultithreadEventLoopGroup multiGroup;
        private final String id;

        IoThreadGroupMetricImpl(MultithreadEventLoopGroup multiGroup, String id) {
            this.multiGroup = multiGroup;
            this.id = id;
        }

        @Override
        public boolean isShutdown() {
            return multiGroup.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return multiGroup.isTerminated();
        }

        @Override
        public List<IoThreadMetric> childExecutors() {
            final List<IoThreadMetric> metrics = new LinkedList<>();

            for (EventExecutor executor : multiGroup) {
                //get metrics for single scheduler
                if (executor instanceof SingleThreadEventLoop) {
                    final IoThreadMetricImpl subMetrics = new IoThreadMetricImpl((SingleThreadEventLoop) executor);
                    metrics.add(subMetrics);
                }
            }

            return metrics;
        }

        @Override
        public String groupId() {
            return id;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", IoThreadGroupMetricImpl.class.getSimpleName()
                    + "[", "]")
                    .add("id='" + id + "'")
                    .add("shutdown=" + isShutdown())
                    .add("terminated=" + isTerminated())
                    .add("childExecutors=" + childExecutors())
                    .toString();
        }
    }

    private static class IoThreadMetricImpl implements IoThreadMetric {

        private final SingleThreadEventLoop eventExecutor;

        private IoThreadMetricImpl(SingleThreadEventLoop eventExecutor) {
            this.eventExecutor = eventExecutor;
        }

        @Override
        public int pendingTasks() {
            return eventExecutor.pendingTasks();
        }

        @Override
        public int maxPendingTasks() {
            return (int) BeanUtils.getFieldValue(eventExecutor, "maxPendingTasks");
        }

        @Override
        public int ioRatio() {
            return (int) BeanUtils.getFieldValue(eventExecutor, "ioRatio");
        }

        @Override
        public String name() {
            return eventExecutor.threadProperties().name();
        }

        @Override
        public int priority() {
            return eventExecutor.threadProperties().priority();
        }

        @Override
        public String state() {
            return eventExecutor.threadProperties().state().name();
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", IoThreadMetricImpl.class.getSimpleName() + "[", "]")
                    .add("name='" + name() + "'")
                    .add("pendingTasks=" + pendingTasks())
                    .add("maxPendingTasks=" + maxPendingTasks())
                    .add("ioRatio=" + ioRatio())
                    .add("priority=" + priority())
                    .add("state='" + state() + "'")
                    .toString();
        }
    }

}
