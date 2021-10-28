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
package io.esastack.httpclient.core.netty;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.concurrent.ThreadFactories;
import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpHeaderValues;
import esa.commons.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpClientBuilder;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.Listener;
import io.esastack.httpclient.core.Scheme;
import io.esastack.httpclient.core.config.ChannelPoolOptions;
import io.esastack.httpclient.core.exec.ExecContext;
import io.esastack.httpclient.core.exec.HttpTransceiver;
import io.esastack.httpclient.core.filter.ResponseFilter;
import io.esastack.httpclient.core.spi.ChannelPoolOptionsProvider;
import io.esastack.httpclient.core.util.Futures;
import io.esastack.httpclient.core.util.LoggerUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.SystemPropertyUtil;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.esastack.httpclient.core.netty.Utils.CONNECT_INACTIVE;
import static io.esastack.httpclient.core.netty.Utils.WRITE_BUF_IS_FULL;

/**
 * The default implementation of {@link HttpTransceiver} which is based on netty.
 */
class HttpTransceiverImpl implements HttpTransceiver {

    private static final String HASHEDWHEELTIMER_TICKDURATION_KEY =
            "io.esastack.httpclient.hashedWheelTimer.tickDurationMs";
    private static final String HASHEDWHEELTIMER_SIZE_KEY =
            "io.esastack.httpclient.hashedWheelTimer.size";

    private static final Timer READ_TIMEOUT_TIMER;
    private static final ServerSelector SERVER_SELECTOR = ServerSelector.DEFAULT;

    private static final H1TransceiverHandle H1_HANDLE = new H1TransceiverHandle();
    private static final H2TransceiverHandle H2_HANDLE = new H2TransceiverHandle();

    private final EventLoopGroup ioThreads;
    private final CachedChannelPools channelPools;
    private final HttpClientBuilder builder;
    private final ChannelPoolFactory channelPoolFactory;

    /**
     * The global channelPoolOptions, as the default value when {@link ChannelPoolOptionsProvider#get(SocketAddress)}
     * is absent.
     */
    private final ChannelPoolOptions channelPoolOptions;
    private final ResponseFilter[] rspFilters;

    static {
        READ_TIMEOUT_TIMER = new HashedWheelTimer(ThreadFactories
                .namedThreadFactory("ESAHttpClient-ReadTimout-Checker-", true),
                SystemPropertyUtil.getLong(HASHEDWHEELTIMER_TICKDURATION_KEY, 30L),
                TimeUnit.MILLISECONDS,
                SystemPropertyUtil.getInt(HASHEDWHEELTIMER_SIZE_KEY, 512));
    }

    HttpTransceiverImpl(EventLoopGroup ioThreads,
                        CachedChannelPools channelPools,
                        HttpClientBuilder builder,
                        ChannelPoolOptions channelPoolOptions,
                        ChannelPoolFactory channelPoolFactory) {
        Checks.checkNotNull(ioThreads, "ioThreads");
        Checks.checkNotNull(channelPools, "channelPools");
        Checks.checkNotNull(builder, "builder");
        Checks.checkNotNull(channelPoolOptions, "channelPoolOptions");
        Checks.checkNotNull(channelPoolFactory, "channelPoolFactory");
        this.ioThreads = ioThreads;
        this.channelPools = channelPools;
        this.builder = builder;
        this.channelPoolOptions = channelPoolOptions;
        this.channelPoolFactory = channelPoolFactory;
        this.rspFilters = builder.buildUnmodifiableResponseFilters();
    }

    @Override
    public CompletableFuture<HttpResponse> handle(HttpRequest request, ExecContext execCtx) {
        final Context ctx = execCtx.ctx();
        final Listener listener = execCtx.listener();
        listener.onFiltersEnd(request, ctx);

        final SocketAddress address = selectServer(request, ctx);

        final ChannelPool channelPool;
        try {
            channelPool = getChannelPool(request, execCtx, address);
        } catch (Throwable ex) {
            onAcquireChannelPoolFailure(request, execCtx, address, ex);
            return Futures.completed(ex);
        }

        listener.onConnectionAttempt(request, ctx, address);
        Future<Channel> channel = channelPool.acquire();

        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        channel.addListener(future -> {
            if (future.isSuccess()) {
                onAcquireChannelSuccess(request, execCtx, address, channelPool, channel.getNow(), response);
            } else {
                onAcquireChannelFailure(request, address, execCtx, response, future.cause());
            }
        });

        return response;
    }

    protected ChannelPool getChannelPool(HttpRequest request, ExecContext execCtx,
                                         SocketAddress address) {
        execCtx.listener().onConnectionPoolAttempt(request, execCtx.ctx(), address);

        final boolean keepAlive = isKeepAlive(request);

        // short-connection hasn't been cached.
        final io.esastack.httpclient.core.netty.ChannelPool channelPool = keepAlive
                ? channelPools.getIfPresent(address) : null;

        final ChannelPool underlying;
        if (channelPool != null) {
            underlying = channelPool.underlying;
        } else {
            boolean ssl = Scheme.HTTPS.name0().equals(request.scheme());
            underlying = channelPools.getOrCreate(keepAlive, address,
                    (addr) -> channelPoolFactory.create(ssl, keepAlive, addr, ioThreads,
                            detectOptions(address), builder))
                    .underlying;
        }

        execCtx.listener().onConnectionPoolAcquired(request, execCtx.ctx(), address);
        return underlying;
    }

    protected void onAcquireChannelPoolFailure(HttpRequest request, ExecContext execCtx,
                                               SocketAddress address, Throwable cause) {
        execCtx.listener().onAcquireConnectionPoolFailed(request, execCtx.ctx(), address, cause);
    }

    /**
     * Callback when failed to acquire channel.
     *
     * <b>IMPORTANT</b>: must release the channel to channelPool in any condition.
     *
     * @param request   request
     * @param execCtx   ctx
     * @param address   address
     * @param channelPool   channelPool
     * @param channel   channel
     * @param response  response
     */
    protected void onAcquireChannelSuccess(HttpRequest request, ExecContext execCtx,
                                           SocketAddress address, ChannelPool channelPool,
                                           Channel channel, CompletableFuture<HttpResponse> response) {
        execCtx.listener().onConnectionAcquired(request, execCtx.ctx(), address);

        boolean http2 = isHttp2(channel);
        esa.commons.http.HttpVersion version;
        if (http2) {
            version = esa.commons.http.HttpVersion.HTTP_2;
        } else {
            version = (esa.commons.http.HttpVersion.HTTP_1_0 == builder.version()
                    ? esa.commons.http.HttpVersion.HTTP_1_0 : esa.commons.http.HttpVersion.HTTP_1_1);
        }

        if (!channel.isActive()) {
            onChannelInactive(request, execCtx, channel, channelPool, response);
            return;
        }

        // Stop writing when the write buffer has full, otherwise it will cause OOM
        if (!channel.isWritable()) {
            onChannelUnWritable(request, execCtx, channel, channelPool, response);
            return;
        }

        HandleRegistry registry;
        try {
            registry = detectRegistry(channel);
            doWrite(request, execCtx, http2, version, channel, channelPool, registry, response);
        } catch (Throwable th) {
            channelPool.release(channel);
            completeExceptionally(request, execCtx, response, th);
        }
    }

    protected void onAcquireChannelFailure(HttpRequest request, SocketAddress address,
                                           ExecContext execCtx, CompletableFuture<HttpResponse> response,
                                           Throwable cause) {
        // Maybe caused by too many acquires or channel has closed.
        Throwable ex = new ConnectException(cause.getMessage());
        response.completeExceptionally(ex);
        execCtx.listener().onAcquireConnectionFailed(request, execCtx.ctx(), address, ex);
    }

    protected void onChannelInactive(HttpRequest request, ExecContext execContext,
                                     Channel channel, ChannelPool channelPool,
                                     CompletableFuture<HttpResponse> response) {
        channel.close();
        channelPool.release(channel);
        completeExceptionally(request, execContext, response, CONNECT_INACTIVE);
    }

    protected void onChannelUnWritable(HttpRequest request, ExecContext execContext,
                                       Channel channel, ChannelPool channelPool,
                                       CompletableFuture<HttpResponse> response) {
        channelPool.release(channel);
        // Allow to retry on another channel
        completeExceptionally(request, execContext, response, WRITE_BUF_IS_FULL);
    }

    /**
     * Just prepare to write the given {@code request} and complete the given {@code response}.
     *
     * <b>IMPORTANT</b>: must release the channel to channelPool in any condition.
     *
     * @param request   request
     * @param execCtx   execCtx
     * @param http2     http2
     * @param version   version, may be http1.0/1.1 or http2.
     * @param channel   channel
     * @param channelPool channelPool
     * @param registry  registry
     * @param response  response
     */
    protected void doWrite(HttpRequest request,
                           ExecContext execCtx,
                           boolean http2,
                           esa.commons.http.HttpVersion version,
                           Channel channel,
                           ChannelPool channelPool,
                           HandleRegistry registry,
                           CompletableFuture<HttpResponse> response) {
        final TimeoutHandle handle = buildTimeoutHandle(http2, channel, channelPool, execCtx.listener(), version);
        setKeepAliveIfNecessary((Http1HeadersImpl) request.headers(), version);

        // Note: we should add response handle before writing because that the inbound
        // message may arrive before completing writing.
        final int requestId = addRspHandle(request, execCtx, channel, http2, registry, handle, response);

        try {
            handle.onWriteAttempt(request, execCtx.ctx());

            final RequestWriter writer = detectWriter(request);
            final ChannelPromise headFuture = channel.newPromise();

            final ChannelFuture endFuture = writer.writeAndFlush(request,
                    channel,
                    execCtx,
                    headFuture,
                    request.uriEncode(),
                    esa.commons.http.HttpVersion.HTTP_1_1 == version
                            ? HttpVersion.HTTP_1_1 : HttpVersion.HTTP_1_0,
                    http2);

            afterWriting(requestId, request, execCtx, writer, headFuture, endFuture, handle, registry, response);
        } catch (Throwable th) {
            tryToCleanAndEndExceptionally(request, execCtx, requestId, registry, handle, response, th);
        }
    }

    /**
     * Adds read-timeout-task after writing.
     *
     * @param requestId requestId
     * @param request   request
     * @param execCtx   ctx
     * @param writer    writer
     * @param headFuture    headFuture
     * @param endFuture endFuture
     * @param handle    handle, which can release channel by proxying {@link Listener}.
     * @param registry  registry
     * @param response  response
     */
    protected void afterWriting(int requestId,
                                HttpRequest request,
                                ExecContext execCtx,
                                RequestWriter writer,
                                ChannelFuture headFuture,
                                ChannelFuture endFuture,
                                TimeoutHandle handle,
                                HandleRegistry registry,
                                CompletableFuture<HttpResponse> response) {
        Timeout timeout = READ_TIMEOUT_TIMER.newTimeout(new ReadTimeoutTask(requestId,
                        request.uri().toString(),
                        request.readTimeout(),
                        headFuture.channel(),
                        registry),
                TimeUnit.MILLISECONDS.toNanos(request.readTimeout()),
                TimeUnit.NANOSECONDS);
        handle.addTimeoutTask(timeout);

        headFuture.addListener(future -> endFuture.addListener(future1 ->
                onWriteDone(request, execCtx, requestId, registry, headFuture.channel(),
                        future, future1, handle, response)));
    }

    protected void onWriteDone(HttpRequest request,
                               ExecContext execCtx,
                               int requestId,
                               HandleRegistry registry,
                               Channel channel,
                               Future<?> headFuture,
                               Future<?> endFuture,
                               TimeoutHandle handle,
                               CompletableFuture<HttpResponse> response) {
        if (headFuture.isSuccess() && endFuture.isSuccess()) {
            handle.onWriteDone(request, execCtx.ctx());
            return;
        }

        final Throwable cause;
        if (!headFuture.isSuccess()) {
            // If we failed to write headers to channel, then we build a connect exception
            // so that we can default to retry again.
            cause = new ConnectException(headFuture.cause().getMessage());
        } else {
            // If we has wrote headers successfully but failed to write data, then we won't
            // use default retry corresponding idempotence.
            cause = new IOException("Failed to write request: " + request + " to connection: "
                    + channel, endFuture.cause());
        }

        handle.onWriteFailed(request, execCtx.ctx(), endFuture.cause());
        tryToCleanAndEndExceptionally(request, execCtx, requestId, registry, handle, response, cause);
    }

    protected void completeExceptionally(HttpRequest request, ExecContext execCtx,
                                         CompletableFuture<HttpResponse> response,
                                         Throwable cause) {
        response.completeExceptionally(cause);
        execCtx.listener().onError(request, execCtx.ctx(), cause);
    }

    /**
     * Try to end and clean {@link ResponseHandle} exceptionally.
     *
     * The main logic described as:
     * 1. If the requestId has been added to {@link HandleRegistry} then we try to remove and end it.
     * 2. If the requestId hasn't been added to {@link HandleRegistry} then we just end the {@link TimeoutHandle}
     *    directly.
     *
     * @param request   request
     * @param execCtx   execCtx
     * @param requestId requestId
     * @param registry  registry
     * @param handle    handle
     * @param response  response
     * @param cause     cause
     */
    protected void tryToCleanAndEndExceptionally(HttpRequest request, ExecContext execCtx,
                                                 int requestId, HandleRegistry registry,
                                                 TimeoutHandle handle,
                                                 CompletableFuture<HttpResponse> response,
                                                 Throwable cause) {
        if (registry != null && requestId != -1) {
            ResponseHandle handle0 = registry.remove(requestId);
            if (handle0 != null) {
                handle0.onError(cause);
            }
            return;
        }

        if (response.completeExceptionally(cause)) {
            handle.onError(request, execCtx.ctx(), cause);
        }
    }

    protected RequestWriter detectWriter(HttpRequest request) {
        if (request.isSegmented()) {
            return new SegmentWriter();
        }
        if (request.isMultipart()) {
            return MultipartWriter.singleton();
        }
        if (request.file() != null) {
            return FileWriter.singleton();
        }
        return PlainWriter.singleton();
    }

    /**
     * Designed as package visibility for unit test purpose.
     *
     * @param address       address
     * @return              options
     */
    ChannelPoolOptions detectOptions(SocketAddress address) {
        ChannelPoolOptionsProvider provider;
        ChannelPoolOptions channelPoolOptions = null;
        if ((provider = builder.channelPoolOptionsProvider()) != null) {
            channelPoolOptions = provider.get(address);
        }
        if (channelPoolOptions != null) {
            return channelPoolOptions;
        }

        return this.channelPoolOptions;
    }

    static void closeTimer() {
        final long start = System.nanoTime();
        final Set<Timeout> tasks = READ_TIMEOUT_TIMER.stop();
        LoggerUtils.logger().info("Begin to close readTimeout-Timer, unfinished tasks size: {}", tasks.size());
        for (Timeout item : tasks) {
            if (item.task() instanceof ReadTimeoutTask) {
                ((ReadTimeoutTask) item.task()).cancel();
            }
        }
        LoggerUtils.logger().info("Closed readTimeout-Timer successfully and all" +
                " unfinished tasks has been canceled, time elapsed: {}", (System.nanoTime() - start) / 1_000_000);
    }

    private boolean isHttp2(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        return pipeline.get(Http2ConnectionHandler.class) != null;
    }

    private boolean isKeepAlive(HttpRequest request) {
        final String value = request.headers().get(HttpHeaderNames.CONNECTION);
        if (!StringUtils.isEmpty(value)) {
            if (HttpHeaderValues.CLOSE.equalsIgnoreCase(value)) {
                return false;
            } else if (HttpHeaderValues.KEEP_ALIVE.equalsIgnoreCase(value)) {
                return true;
            }

        }
        return builder.isKeepAlive();
    }

    private TimeoutHandle buildTimeoutHandle(boolean http2,
                                             Channel channel,
                                             ChannelPool channelPool,
                                             Listener delegate,
                                             esa.commons.http.HttpVersion version) {
        if (http2) {
            return H2_HANDLE.buildTimeoutHandle(channel, channelPool, delegate,
                    esa.commons.http.HttpVersion.HTTP_2);
        }

        return H1_HANDLE.buildTimeoutHandle(channel, channelPool, delegate, version);
    }

    private int addRspHandle(HttpRequest request,
                             ExecContext execCtx,
                             Channel channel,
                             boolean http2,
                             HandleRegistry registry,
                             TimeoutHandle handle,
                             CompletableFuture<HttpResponse> response) {
        if (http2) {
            return H2_HANDLE.addRspHandle(
                    request,
                    execCtx,
                    channel,
                    rspFilters,
                    registry,
                    handle,
                    response);
        } else {
            return H1_HANDLE.addRspHandle(
                    request,
                    execCtx,
                    channel,
                    rspFilters,
                    registry,
                    handle,
                    response);
        }
    }

    private HandleRegistry detectRegistry(Channel channel) throws ConnectException {
        ChannelPipeline pipeline = channel.pipeline();
        Http1ChannelHandler handler1;
        if ((handler1 = pipeline.get(Http1ChannelHandler.class)) != null) {
            return handler1.getRegistry();
        }

        Http2ConnectionHandler handler2;
        if ((handler2 = pipeline.get(Http2ConnectionHandler.class)) != null) {
            return handler2.getRegistry();
        }

        throw CONNECT_INACTIVE;
    }

    private static SocketAddress selectServer(HttpRequest request, Context ctx) {
        return SERVER_SELECTOR.select(request, ctx);
    }

    /**
     * Set keepAlive to given headers.
     *
     * @param headers   headers
     * @param version   version
     */
    private void setKeepAliveIfNecessary(Http1HeadersImpl headers, esa.commons.http.HttpVersion version) {
        if (esa.commons.http.HttpVersion.HTTP_2 == builder.version()) {
            headers.remove(HttpHeaderNames.CONNECTION);
        }

        if (headers.contains(HttpHeaderNames.CONNECTION)) {
            return;
        }

        final boolean keepAlive = builder.isKeepAlive();
        HttpUtil.setKeepAlive(headers,
                esa.commons.http.HttpVersion.HTTP_1_1 == version
                        ? HttpVersion.HTTP_1_1 : HttpVersion.HTTP_1_0,
                keepAlive);
    }

}
