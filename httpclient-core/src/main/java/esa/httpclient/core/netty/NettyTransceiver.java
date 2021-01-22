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
import esa.commons.StringUtils;
import esa.commons.concurrent.ThreadFactories;
import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpHeaderValues;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.httpclient.core.Context;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.Scheme;
import esa.httpclient.core.SegmentRequest;
import esa.httpclient.core.config.SslOptions;
import esa.httpclient.core.exception.WriteBufFullException;
import esa.httpclient.core.exec.HttpTransceiver;
import esa.httpclient.core.filter.ResponseFilter;
import esa.httpclient.core.spi.SslEngineFactory;
import esa.httpclient.core.util.Futures;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.SystemPropertyUtil;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

import static esa.httpclient.core.netty.Utils.CONNECT_INACTIVE;
import static esa.httpclient.core.netty.Utils.getValue;

class NettyTransceiver implements HttpTransceiver {

    private static final String HASHEDWHEELTIMER_TICKDURATION_KEY = "esa.httpclient.hashedWheelTimer.tickDurationMs";
    private static final String HASHEDWHEELTIMER_SIZE_KEY = "esa.httpclient.hashedWheelTimer.size";

    private static final Timer READ_TIMEOUT_TIMER;
    private static final ServerSelector SERVER_SELECTOR = ServerSelector.DEFAULT;

    private static final H1TransceiverHandle H1_HANDLE = new H1TransceiverHandle();
    private static final H2TransceiverHandle H2_HANDLE = new H2TransceiverHandle();

    private final EventLoopGroup ioThreads;
    private final ChannelPools channelPools;
    private final HttpClientBuilder builder;
    private final SslEngineFactory sslEngineFactory;
    private final ResponseFilter[] rspFilters;

    static {
        READ_TIMEOUT_TIMER = new HashedWheelTimer(ThreadFactories
                .namedThreadFactory("HttpClient-ReadTimout-Checker-", true),
                SystemPropertyUtil.getLong(HASHEDWHEELTIMER_TICKDURATION_KEY, 30L),
                TimeUnit.MILLISECONDS,
                SystemPropertyUtil.getInt(HASHEDWHEELTIMER_SIZE_KEY, 512));
    }

    NettyTransceiver(EventLoopGroup ioThreads,
                     ChannelPools channelPools,
                     HttpClientBuilder builder,
                     SslEngineFactory sslEngineFactory) {
        Checks.checkNotNull(ioThreads, "IOThreads must not be null");
        Checks.checkNotNull(channelPools, "ChannelPools must not be null");
        Checks.checkNotNull(builder, "HttpClientBuilder must not be null");
        Checks.checkNotNull(sslEngineFactory, "SslEngineFactory must not be null");
        this.ioThreads = ioThreads;
        this.channelPools = channelPools;
        this.builder = builder;
        this.sslEngineFactory = sslEngineFactory;
        this.rspFilters = builder.buildUnmodifiableResponseFilters();
    }

    @Override
    public CompletableFuture<HttpResponse> handle(HttpRequest request,
                                                  Context ctx,
                                                  BiFunction<Listener, CompletableFuture<HttpResponse>,
                                                          HandleImpl> handle,
                                                  final Listener listener) {
        listener.onFiltersEnd(request, ctx);

        final SocketAddress address = selectServer(request, ctx);
        ChannelPool channelPool;
        listener.onConnectionPoolAttempt(request, ctx, address);

        // Saves segment write for further using.
        final CompletableFuture<SegmentWriter> segmentWriterPromise;
        if (request instanceof SegmentRequest) {
            segmentWriterPromise = new CompletableFuture<>();
            ((NettyContext) ctx).setWriter(segmentWriterPromise);
        } else {
            segmentWriterPromise = null;
        }

        try {
            channelPool = getChannelPool(request, address);
        } catch (Throwable ex) {
            listener.onAcquireConnectionPoolFailed(request, ctx, address, ex);
            endRequestWriter(segmentWriterPromise, ex);
            return Futures.completed(ex);
        }

        listener.onConnectionPoolAcquired(request, ctx, address);
        listener.onConnectionAttempt(request, ctx, address);

        final RequestWriter writer = detectWriter(request);
        final Future<Channel> channel = channelPool.acquire();

        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        if (channel.isDone()) {
            this.handle0(request,
                    address,
                    ctx,
                    channelPool,
                    channel,
                    handle,
                    listener,
                    response,
                    writer,
                    segmentWriterPromise);
        } else {
            channel.addListener(channel0 -> this.handle0(request,
                    address,
                    ctx,
                    channelPool,
                    channel,
                    handle,
                    listener,
                    response,
                    writer,
                    segmentWriterPromise));
        }

        return response;
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

    private static SocketAddress selectServer(HttpRequest request, Context ctx) {
        return SERVER_SELECTOR.select(request, ctx);
    }

    void handle0(HttpRequest request,
                 SocketAddress address,
                 Context ctx,
                 ChannelPool channelPool,
                 Future<Channel> channel,
                 BiFunction<Listener, CompletableFuture<HttpResponse>, HandleImpl> handle,
                 Listener listener,
                 CompletableFuture<HttpResponse> response,
                 RequestWriter writer,
                 CompletableFuture<SegmentWriter> segmentWriterPromise) {
        if (!channel.isSuccess()) {
            this.onAcquireConnectionFailed(request,
                    address,
                    ctx,
                    channel,
                    listener,
                    response,
                    segmentWriterPromise);
            return;
        }

        final Channel channel0 = channel.getNow();
        try {
            this.doWrite(request,
                    ctx,
                    channelPool,
                    channel0,
                    handle,
                    listener,
                    response,
                    writer,
                    segmentWriterPromise);
        } catch (Throwable th) {
            channelPool.release(channel0);
            endWithError(request, ctx, listener, response, segmentWriterPromise,
                    channel0.isActive() ? th : CONNECT_INACTIVE);
        }
    }

    void doWrite(HttpRequest request,
                 Context ctx,
                 ChannelPool channelPool,
                 Channel channel,
                 BiFunction<Listener, CompletableFuture<HttpResponse>, HandleImpl> handle,
                 Listener listener,
                 CompletableFuture<HttpResponse> response,
                 RequestWriter writer,
                 CompletableFuture<SegmentWriter> segmentWriterPromise) throws ConnectException {
        listener.onConnectionAcquired(request, ctx, channel.remoteAddress());

        final boolean http2 = isHttp2(channel);
        final esa.commons.http.HttpVersion version;
        if (http2) {
            version = esa.commons.http.HttpVersion.HTTP_2;
        } else {
            version = (esa.commons.http.HttpVersion.HTTP_1_0 == builder.version()
                    ? esa.commons.http.HttpVersion.HTTP_1_0 : esa.commons.http.HttpVersion.HTTP_1_1);
        }

        if (!channel.isActive()) {
            channel.close();
            channelPool.release(channel);
            endWithError(request, ctx, listener, response, segmentWriterPromise,
                    CONNECT_INACTIVE);
            return;
        }

        // Stop writing when the write buffer has full, otherwise it will cause OOM
        if (!channel.isWritable()) {
            channelPool.release(channel);
            // Allow to retry on another channel
            endWithError(request, ctx, listener, response, segmentWriterPromise,
                    WriteBufFullException.INSTANCE);
            return;
        }

        try {
            TimeoutHandle h = buildTimeoutHandle(http2, channel, channelPool, listener, version);
            this.doWrite0(request,
                    ctx,
                    channel,
                    handle,
                    h,
                    http2,
                    version,
                    response,
                    writer,
                    segmentWriterPromise);
        } catch (Throwable ex) {
            channelPool.release(channel);
            endWithError(request, ctx, listener, response, segmentWriterPromise,
                    channel.isActive() ? ex : CONNECT_INACTIVE);
        }
    }

    private void onAcquireConnectionFailed(HttpRequest request,
                                           SocketAddress address,
                                           Context ctx,
                                           Future<Channel> channel,
                                           Listener listener,
                                           CompletableFuture<HttpResponse> response,
                                           CompletableFuture<SegmentWriter> segmentWriterPromise) {
        Throwable cause = channel.cause();

        // Maybe caused by too many acquires or channel has closed.
        if (cause instanceof IllegalStateException) {
            cause = new IOException("Error while acquiring connection", cause);
        } else if (cause instanceof TimeoutException) {
            // Connection timeout
            cause = new ConnectException(cause.getMessage());
        }

        response.completeExceptionally(cause);
        endRequestWriter(segmentWriterPromise, cause);

        listener.onAcquireConnectionFailed(request, ctx, address, cause);
        listener.onError(request, ctx, cause);
    }

    void doWrite0(HttpRequest request,
                  Context ctx,
                  Channel channel,
                  BiFunction<Listener, CompletableFuture<HttpResponse>, HandleImpl> handle,
                  final TimeoutHandle h,
                  boolean http2,
                  esa.commons.http.HttpVersion version,
                  CompletableFuture<HttpResponse> response,
                  RequestWriter writer,
                  CompletableFuture<SegmentWriter> segmentWriterPromise) throws IOException {
        final HandleRegistry registry = detectRegistry(channel);
        setKeepAliveIfNecessary((Http1HeadersImpl) request.headers(), version);

        h.onWriteAttempt(request, ctx);

        // Note: we should add response handle before writing because that the inbound
        // message may arrive before completing writing.
        final int requestId = addRspHandle(request,
                ctx,
                channel,
                h,
                handle.apply(h, response),
                http2,
                registry,
                response);
        final ChannelPromise headFuture = channel.newPromise();
        @SuppressWarnings("unchecked") final ChannelFuture endFuture = writer.writeAndFlush(request,
                channel,
                ctx,
                headFuture,
                getValue(request.uriEncodeEnabled(), builder.isUriEncodeEnabled()),
                esa.commons.http.HttpVersion.HTTP_1_1 == version
                        ? HttpVersion.HTTP_1_1 : HttpVersion.HTTP_1_0,
                http2);

        if (segmentWriterPromise != null) {
            segmentWriterPromise.complete((SegmentWriter) writer);
        }

        if (endFuture.isDone()) {
            this.onWriteDone(requestId,
                    request,
                    ctx,
                    headFuture,
                    endFuture,
                    h,
                    registry,
                    response,
                    segmentWriterPromise);
        } else {
            endFuture.addListener(f -> {
                try {
                    this.onWriteDone(requestId,
                            request,
                            ctx,
                            headFuture,
                            endFuture,
                            h,
                            registry,
                            response,
                            segmentWriterPromise);
                } catch (Throwable ex) {
                    endWithError(request, ctx, h, response, segmentWriterPromise, ex);
                }
            });
        }
    }

    ChannelPool getChannelPool(HttpRequest request, SocketAddress address) {
        final boolean keepAlive = isKeepAlive(request);

        esa.httpclient.core.netty.ChannelPool channelPool = keepAlive ? channelPools.getIfPresent(address) : null;
        if (channelPool != null) {
            return channelPool.underlying;
        }

        final boolean ssl = Scheme.HTTPS.name0().equals(request.scheme());
        return channelPools.getOrCreate(ssl,
                keepAlive,
                address,
                ioThreads,
                builder.copy(),
                () -> {
                    final SslOptions sslOptions = builder.sslOptions();
                    SSLEngine sslEngine = sslEngineFactory.create(sslOptions,
                            ((InetSocketAddress) address).getHostName(),
                            ((InetSocketAddress) address).getPort() > 0
                                    ? ((InetSocketAddress) address).getPort()
                                    : ssl ? Scheme.HTTPS.port() : Scheme.HTTP.port());
                    if (sslOptions != null && sslOptions.enabledProtocols().length > 0) {
                        sslEngine.setEnabledProtocols(sslOptions.enabledProtocols());
                    }

                    SslHandler sslHandler = new SslHandler(sslEngine);
                    if (sslOptions != null && sslOptions.handshakeTimeoutMillis() > 0) {
                        sslHandler.setHandshakeTimeoutMillis(sslOptions.handshakeTimeoutMillis());
                    } else {
                        int connectTimeout = builder.connectTimeout();
                        if (connectTimeout > 0) {
                            sslHandler.setHandshakeTimeoutMillis(Duration.ofSeconds(connectTimeout).toMillis());
                        }
                    }

                    return sslHandler;
                }).underlying;
    }

    static RequestWriter detectWriter(HttpRequest request) {
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

    private boolean isHttp2(Channel channel) throws ConnectException {
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get(Http2ConnectionHandler.class) != null) {
            return true;
        } else if (pipeline.get(Http1ChannelHandler.class) != null) {
            return false;
        }

        throw CONNECT_INACTIVE;
    }

    private boolean isKeepAlive(HttpRequest request) {
        final String value = request.headers().get(HttpHeaderNames.CONNECTION);
        if (StringUtils.isEmpty(value)) {
            return builder.isKeepAlive();
        } else {
            if (HttpHeaderValues.CLOSE.equalsIgnoreCase(value)) {
                return false;
            } else if (HttpHeaderValues.KEEP_ALIVE.equalsIgnoreCase(value)) {
                return true;
            }

            return builder.isKeepAlive();
        }
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
                             Context ctx,
                             Channel channel,
                             Listener listener,
                             HandleImpl handle,
                             boolean http2,
                             HandleRegistry registry,
                             CompletableFuture<HttpResponse> response) {
        if (http2) {
            return H2_HANDLE.addRspHandle(
                    request,
                    ctx,
                    channel,
                    listener,
                    handle,
                    rspFilters,
                    registry,
                    response);
        } else {
            return H1_HANDLE.addRspHandle(
                    request,
                    ctx,
                    channel,
                    listener,
                    handle,
                    rspFilters,
                    registry,
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

    private void onWriteDone(int requestId,
                             HttpRequest request,
                             Context ctx,
                             ChannelFuture headFuture,
                             ChannelFuture endFuture,
                             TimeoutHandle handle,
                             HandleRegistry registry,
                             CompletableFuture<HttpResponse> response,
                             CompletableFuture<SegmentWriter> segmentWriterPromise) {
        if (endFuture.isSuccess()) {
            handle.onWriteDone(request, ctx);

            Timeout timeout = READ_TIMEOUT_TIMER.newTimeout(new ReadTimeoutTask(requestId,
                            request.uri().toString(),
                            endFuture.channel(),
                            registry),
                    TimeUnit.MILLISECONDS.toNanos(request.readTimeout()),
                    TimeUnit.NANOSECONDS);
            handle.addCancelTask(timeout);
            return;
        }

        final Throwable cause;
        if (headFuture.isDone() && !headFuture.isSuccess()) {
            // Note: we instantiate a ConnectException if have failed to write header, so
            // that we can retry current request soon without worrying server idempotent.
            cause = new ConnectException(endFuture.cause().getMessage());
        } else {
            cause = new IOException("Failed to write request: " + request + " to connection: "
                    + endFuture.channel(), endFuture.cause());
        }

        handle.onWriteFailed(request, ctx, endFuture.cause());
        endWithError(request, ctx, handle, response, segmentWriterPromise, cause);
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

    private static void endWithError(HttpRequest request,
                                     Context ctx,
                                     Listener listener,
                                     CompletableFuture<HttpResponse> response,
                                     CompletableFuture<SegmentWriter> segmentWriterPromise,
                                     Throwable cause) {
        response.completeExceptionally(cause);
        if (segmentWriterPromise != null) {
            segmentWriterPromise.completeExceptionally(cause);
        }
        listener.onError(request, ctx, cause);
    }

    private static void endRequestWriter(CompletableFuture<SegmentWriter> requestWriterPromise,
                                         Throwable cause) {
        if (requestWriterPromise == null) {
            return;
        }

        requestWriterPromise.completeExceptionally(cause);
    }

}
