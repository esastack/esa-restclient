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

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.ExecContextUtil;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.HttpClientBuilder;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.HttpUri;
import io.esastack.httpclient.core.Listener;
import io.esastack.httpclient.core.config.ChannelPoolOptions;
import io.esastack.httpclient.core.config.SslOptions;
import io.esastack.httpclient.core.exec.ExecContext;
import io.esastack.httpclient.core.spi.ChannelPoolOptionsProvider;
import io.esastack.httpclient.core.spi.SslEngineFactory;
import io.esastack.httpclient.core.util.Futures;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HttpTransceiverImplTest {

    private final HttpClient client = HttpClient.ofDefault();

    @Test
    void testConstructor() {
        final EventLoopGroup ioThreads = mock(EventLoopGroup.class);
        final CachedChannelPools channelPools = mock(CachedChannelPools.class);
        final HttpClientBuilder builder = HttpClient.create();
        final ChannelPoolOptions options = ChannelPoolOptions.ofDefault();
        final ChannelPoolFactory channelPoolFactory = new ChannelPoolFactory(mock(SslEngineFactory.class));

        assertThrows(NullPointerException.class, () -> new HttpTransceiverImpl(null,
                channelPools, builder, options, channelPoolFactory));

        assertThrows(NullPointerException.class, () -> new HttpTransceiverImpl(ioThreads,
                null, builder, options, channelPoolFactory));

        assertThrows(NullPointerException.class, () -> new HttpTransceiverImpl(ioThreads,
                channelPools, null, options, channelPoolFactory));

        assertThrows(NullPointerException.class, () -> new HttpTransceiverImpl(ioThreads,
                channelPools, builder, null, channelPoolFactory));

        assertThrows(NullPointerException.class, () -> new HttpTransceiverImpl(ioThreads,
                channelPools, builder, options, null));

        assertDoesNotThrow(() -> new HttpTransceiverImpl(ioThreads,
                channelPools, builder, options, channelPoolFactory));
    }

    @Test
    void testHandle() {
        final CachedChannelPools channelPools1 = mock(CachedChannelPools.class);
        final HttpTransceiverImpl transceiver1 = setUp(channelPools1);
        final HttpRequest request = client.get("http://127.0.0.1:8080/abc").segment();

        // Case 1: Error while acquiring channelPool
        final Listener listener1 = mock(Listener.class);
        final NettyExecContext ctx1 = ExecContextUtil.from(new Context(), listener1);
        when(channelPools1.getIfPresent(any())).thenThrow(new RuntimeException());

        final CompletableFuture<HttpResponse> response1 = transceiver1.handle(request, ctx1);
        then(response1.isDone() && response1.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response1)).isInstanceOf(RuntimeException.class);

        verify(listener1).onFiltersEnd(any(), any());
        verify(listener1).onConnectionPoolAttempt(any(), any(), any());
        verify(listener1).onAcquireConnectionPoolFailed(any(), any(), any(), any());

        // Case 2: Acquired channelPool successfully but failed to acquire channel.
        final CachedChannelPools channelPools2 = mock(CachedChannelPools.class);
        final HttpTransceiverImpl transceiver2 = setUp(channelPools2);
        final io.netty.channel.pool.ChannelPool underlying = mock(io.netty.channel.pool.ChannelPool.class);
        final io.esastack.httpclient.core.netty.ChannelPool channelPool =
                new io.esastack.httpclient.core.netty.ChannelPool(
                        false, underlying,
                        ChannelPoolOptions.ofDefault());
        when(channelPools2.getIfPresent(any())).thenReturn(channelPool);

        final Channel channel2 = new EmbeddedChannel();
        final ChannelFuture future2 = channel2.newFailedFuture(new IllegalStateException());
        when(underlying.acquire()).thenAnswer(answer -> future2);
        final Listener listener2 = mock(Listener.class);
        final NettyExecContext ctx2 = ExecContextUtil.from(new Context(), listener2);

        final CompletableFuture<HttpResponse> response2 = transceiver2.handle(request, ctx2);
        try {
            channel2.closeFuture().get();
        } catch (Throwable ex) {
            // ignore, for purpose of waiting until future complete.
        }
        then(response2.isDone() && response2.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response2)).isInstanceOf(IOException.class);
        verify(listener2).onFiltersEnd(any(), any());
        verify(listener2).onConnectionPoolAttempt(any(), any(), any());
        verify(listener2).onConnectionPoolAcquired(any(), any(), any());
        verify(listener2).onConnectionAttempt(any(), any(), any());
        verify(listener2).onAcquireConnectionFailed(any(), any(), any(), any());
    }

    @Test
    void testOnAcquireChannelSuccess() {
        final HttpTransceiverImpl transceiver = setUp();
        final HttpRequest request = client.get("http://127.0.0.1:8080/abc").segment();
        final Listener listener = mock(Listener.class);
        final ExecContext ctx = ExecContextUtil.from(new Context(), listener);

        final io.netty.channel.pool.ChannelPool channelPool = mock(io.netty.channel.pool.ChannelPool.class);
        final CompletableFuture<HttpResponse> response1 = new CompletableFuture<>();

        final Channel channel = mock(Channel.class);
        final ChannelPipeline pipeline = mock(ChannelPipeline.class);
        when(channel.pipeline()).thenReturn(pipeline);
        when(pipeline.get(Http2ConnectionHandler.class)).thenReturn(mock(Http2ConnectionHandler.class));

        channel.pipeline().addLast(mock(Http2ConnectionHandler.class));

        // Case 1: channel inactive
        when(channel.isActive()).thenReturn(false);

        transceiver.onAcquireChannelSuccess(request, ctx,
                ServerSelector.DEFAULT.select(request, ctx.ctx()), channelPool, channel, response1);
        verify(channel).close();
        verify(channelPool).release(any());
        then(response1.isDone() && response1.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response1)).isInstanceOf(ConnectException.class);
        verify(listener).onError(any(), any(), any());

        clearInvocations(channelPool);
        // Case 2: channel is unWritable
        when(pipeline.get(Http2ConnectionHandler.class)).thenReturn(null);
        when(pipeline.get(Http1ChannelHandler.class)).thenReturn(mock(Http1ChannelHandler.class));
        when(channel.isActive()).thenReturn(true);
        when(channel.isWritable()).thenReturn(false);

        final Listener listener2 = mock(Listener.class);
        final ExecContext ctx2 = ExecContextUtil.from(new Context(), listener2);
        final CompletableFuture<HttpResponse> response2 = new CompletableFuture<>();

        transceiver.onAcquireChannelSuccess(request, ctx2,
                ServerSelector.DEFAULT.select(request, ctx2.ctx()), channelPool, channel, response2);

        verify(channelPool).release(any());
        then(response2.isDone() && response2.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response2)).isSameAs(Utils.WRITE_BUF_IS_FULL);
        verify(listener2).onError(any(), any(), any());

        clearInvocations(channelPool);
        // Case 3: Error while doWrite
        when(channel.isWritable()).thenReturn(true);
        final Listener listener3 = mock(Listener.class);
        final ExecContext ctx3 = ExecContextUtil.from(new Context(), listener3);
        final CompletableFuture<HttpResponse> response3 = new CompletableFuture<>();

        transceiver.onAcquireChannelSuccess(request, ctx3,
                ServerSelector.DEFAULT.select(request, ctx3.ctx()), channelPool, channel, response3);

        verify(channelPool).release(any());
        then(response3.isDone() && response3.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response3)).isInstanceOf(NullPointerException.class);
        verify(listener3).onError(any(), any(), any());
    }

    @Test
    void testDoWrite() throws Exception {
        final EventLoopGroup ioThreads = mock(EventLoopGroup.class);
        final CachedChannelPools channelPools = mock(CachedChannelPools.class);
        final HttpClientBuilder builder = HttpClient.create();
        final ChannelPoolOptions options = ChannelPoolOptions.ofDefault();
        final ChannelPoolFactory channelPoolFactory = new ChannelPoolFactory(mock(SslEngineFactory.class));
        final io.netty.channel.pool.ChannelPool channelPool = mock(io.netty.channel.pool.ChannelPool.class);
        final Channel channel = new EmbeddedChannel();
        final HandleRegistry registry = new HandleRegistry(1, 0);
        channel.pipeline().addLast(new Http1ChannelHandler(registry, -1L));

        final HttpRequest request = client.get("http://127.0.0.1:8080/abc").segment();
        // Case 1: write successfully
        final TimeoutHandle h1 = mock(TimeoutHandle.class);
        final ExecContext ctx1 = ExecContextUtil.from(new Context(), h1);
        final CompletableFuture<HttpResponse> response1 = new CompletableFuture<>();
        final ChannelFuture future1 = channel.newSucceededFuture();
        final RequestWriter writer1 = mock(RequestWriter.class);
        when(writer1.writeAndFlush(any(HttpRequest.class),
                any(Channel.class),
                any(ExecContext.class),
                any(ChannelPromise.class),
                anyBoolean(),
                any(io.netty.handler.codec.http.HttpVersion.class),
                anyBoolean()))
                .then(answer -> {
                    ChannelPromise promise = answer.getArgument(3);
                    promise.setSuccess();
                    return future1;
                });

        final HttpTransceiverImpl transceiver1 = new HttpTransceiverImpl(ioThreads,
                channelPools,
                builder,
                options,
                channelPoolFactory) {
            @Override
            protected RequestWriter detectWriter(HttpRequest request) {
                return writer1;
            }
        };

        transceiver1.doWrite(request, ctx1, false, HttpVersion.HTTP_1_1,
                channel, channelPool, registry, response1);
        verify(h1).onWriteAttempt(any(), any());
        verify(h1).onWriteDone(any(), any());

        // Case 2: write failure
        final ChannelFuture future2 = channel.newFailedFuture(new IOException());
        final RequestWriter writer2 = mock(RequestWriter.class);
        when(writer2.writeAndFlush(any(HttpRequest.class),
                any(Channel.class),
                any(ExecContext.class),
                any(ChannelPromise.class),
                anyBoolean(),
                any(io.netty.handler.codec.http.HttpVersion.class),
                anyBoolean()))
                .then(answer -> {
                    ChannelPromise promise = answer.getArgument(3);
                    promise.setSuccess();
                    return future2;
                });

        final HttpTransceiverImpl transceiver2 = new HttpTransceiverImpl(ioThreads,
                channelPools,
                builder,
                options,
                channelPoolFactory) {
            @Override
            protected RequestWriter detectWriter(HttpRequest request) {
                return writer2;
            }
        };

        final CompletableFuture<HttpResponse> response2 = new CompletableFuture<>();
        final TimeoutHandle h2 = mock(TimeoutHandle.class);
        final ExecContext ctx2 = ExecContextUtil.from(new Context(), h2);

        transceiver2.doWrite(request, ctx2, false, HttpVersion.HTTP_1_1,
                channel, channelPool, registry, response2);

        verify(h2).onWriteFailed(any(), any(), any());
        verify(h2).onError(any(), any(), any());
        then(response2.isDone() && response2.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response2)).isInstanceOf(IOException.class);
    }

    @Test
    void testGetChannelPool() {
        final HttpClientBuilder builder = HttpClient.create();

        NettyHttpClientTest.NettyHttpClientImpl client = new NettyHttpClientTest.NettyHttpClientImpl(builder,
                true);
        final SslOptions sslOptions = SslOptions
                .options().handshakeTimeoutMillis(2000L).build();

        final ChannelPoolFactory factory = new ChannelPoolFactory(client.loadSslEngineFactory(sslOptions));

        final EventLoopGroup ioThreads = mock(EventLoopGroup.class);
        final CachedChannelPools channelPools = mock(CachedChannelPools.class);

        final HttpTransceiverImpl transceiver = new HttpTransceiverImpl(ioThreads, channelPools, builder,
                ChannelPoolOptions.ofDefault(), factory);
        final HttpRequest request = client.get("http://127.0.0.1:8080/abc");
        final SocketAddress address = InetSocketAddress.createUnresolved("127.0.0.1", 8080);

        final io.netty.channel.pool.ChannelPool underlying1 = mock(io.netty.channel.pool.ChannelPool.class);
        final ChannelPool channelPool1 = new ChannelPool(true, underlying1,
                ChannelPoolOptions.ofDefault());

        when(channelPools.getIfPresent(address)).thenReturn(channelPool1);
        final io.netty.channel.pool.ChannelPool pool1 = transceiver.getChannelPool(request,
                ExecContextUtil.newAs(), address);
        then(pool1).isSameAs(underlying1);

        when(channelPools.getIfPresent(address)).thenReturn(null);

        final AtomicBoolean keepAlive = new AtomicBoolean();
        final AtomicReference<SocketAddress> address0 = new AtomicReference<>();

        when(channelPools.getOrCreate(anyBoolean(),
                any(),
                any()))
                .thenAnswer(answer -> {
                    keepAlive.set(answer.getArgument(0));
                    address0.set(answer.getArgument(1));
                    return channelPool1;
                });

        then(transceiver.getChannelPool(request, ExecContextUtil.newAs(), address)).isSameAs(pool1);
        then(keepAlive.get()).isEqualTo(true);
        then(address0.get()).isEqualTo(address);
    }

    @Test
    void testDetectWriter() {
        final HttpTransceiverImpl transceiver = setUp();

        final HttpRequest request1 = client.get("/abc");
        then(transceiver.detectWriter(request1)).isInstanceOf(PlainWriter.class);

        final HttpRequest request2 = client.post("/abc").body(Buffer.defaultAlloc().buffer());
        then(transceiver.detectWriter(request2)).isInstanceOf(PlainWriter.class);

        final HttpRequest request3 = client.patch("/abc").body(new File(""));
        then(transceiver.detectWriter(request3)).isInstanceOf(FileWriter.class);

        final HttpRequest request4 = client.get("/abc").multipart().attr("", "");
        then(transceiver.detectWriter(request4)).isInstanceOf(MultipartWriter.class);

        final HttpRequest request5 = client.get("/abc").segment();
        then(transceiver.detectWriter(request5)).isInstanceOf(SegmentWriter.class);
    }

    @Test
    void testOnAcquireChannelPoolFailure() {
        final HttpTransceiverImpl transceiver = setUp();
        final HttpRequest request = mock(HttpRequest.class);
        final Listener listener = mock(Listener.class);
        final ExecContext ctx = ExecContextUtil.from(new Context(), listener);

        transceiver.onAcquireChannelPoolFailure(request, ctx, mock(SocketAddress.class), new IOException());
        verify(listener).onAcquireConnectionPoolFailed(any(), any(), any(), any());
    }

    @Test
    void testOnAcquireChannelFailure() {
        final HttpTransceiverImpl transceiver = setUp();
        final HttpRequest request = mock(HttpRequest.class);
        final Listener listener = mock(Listener.class);
        final ExecContext ctx = ExecContextUtil.from(new Context(), listener);
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        transceiver.onAcquireChannelFailure(request, mock(SocketAddress.class), ctx, response, new IOException());
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(ConnectException.class);
        verify(listener).onAcquireConnectionFailed(any(), any(), any(), any());
    }

    @Test
    void testOnChannelInactive() {
        final HttpTransceiverImpl transceiver = setUp();
        final HttpRequest request = mock(HttpRequest.class);
        final Listener listener = mock(Listener.class);
        final ExecContext ctx = ExecContextUtil.from(new Context(), listener);
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final io.netty.channel.pool.ChannelPool channelPool = mock(io.netty.channel.pool.ChannelPool.class);
        final Channel channel = mock(Channel.class);

        transceiver.onChannelInactive(request, ctx, channel, channelPool, response);
        verify(channel).close();
        verify(channelPool).release(channel);
        verify(listener).onError(any(), any(), any());
        then(Futures.getCause(response)).isInstanceOf(ConnectException.class);
    }

    @Test
    void testOnChannelUnWritable() {
        final HttpTransceiverImpl transceiver = setUp();
        final HttpRequest request = mock(HttpRequest.class);
        final Listener listener = mock(Listener.class);
        final ExecContext ctx = ExecContextUtil.from(new Context(), listener);
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final io.netty.channel.pool.ChannelPool channelPool = mock(io.netty.channel.pool.ChannelPool.class);
        final Channel channel = mock(Channel.class);

        transceiver.onChannelUnWritable(request, ctx, channel, channelPool, response);
        verify(channelPool).release(channel);
        verify(listener).onError(any(), any(), any());
        then(Futures.getCause(response)).isInstanceOf(ConnectException.class);
        verify(channel, never()).close();
    }

    @Test
    void testAfterWriting() {
        final HttpTransceiverImpl transceiver = setUp();
        final HttpRequest request = mock(HttpRequest.class);
        when(request.uri()).thenReturn(new HttpUri("/abc"));
        when(request.readTimeout()).thenReturn(Long.MAX_VALUE);

        final Listener listener1 = mock(Listener.class);
        final ExecContext ctx1 = ExecContextUtil.from(new Context(), listener1);
        final CompletableFuture<HttpResponse> response1 = new CompletableFuture<>();
        final Channel channel1 = new EmbeddedChannel();

        final HandleRegistry registry1 = new HandleRegistry(1, 0);
        final TimeoutHandle handle1 = mock(TimeoutHandle.class);
        registry1.put(new ResponseHandle(new HandleImpl(new NettyResponse(true)),
                request, ctx1, handle1, response1));

        // case1: all success
        transceiver.afterWriting(1, request, ctx1, FileWriter.singleton(),
                channel1.newSucceededFuture(), channel1.newSucceededFuture(), handle1, registry1, response1);
        verify(handle1).onWriteDone(any(), any());
        verify(listener1, never()).onError(any(), any(), any());
        assertFalse(response1.isCompletedExceptionally());

        // case2: head fails and data success
        final ExecContext ctx2 = ExecContextUtil.from(new Context(), listener1);
        final CompletableFuture<HttpResponse> response2 = new CompletableFuture<>();
        final Channel channel2 = new EmbeddedChannel();

        final HandleRegistry registry2 = new HandleRegistry(1, 0);
        final TimeoutHandle handle2 = mock(TimeoutHandle.class);
        final ChannelFuture headFuture2 = channel2.newFailedFuture(new IOException());
        final ChannelFuture dataFuture2 = channel2.newSucceededFuture();
        registry2.put(new ResponseHandle(new HandleImpl(new NettyResponse(true)),
                request, ctx2, handle2, response2));

        transceiver.afterWriting(1, request, ctx2, FileWriter.singleton(),
                headFuture2, dataFuture2, handle2, registry2, response2);
        verify(handle2, never()).onWriteDone(any(), any());
        verify(handle2).onWriteFailed(any(), any(), any());
        verify(handle2).onError(any(), any(), any());
        assertTrue(response2.isCompletedExceptionally());
        assertTrue(Futures.getCause(response2) instanceof ConnectException);

        // case3: head fails and data fails
        final ExecContext ctx3 = ExecContextUtil.from(new Context(), listener1);
        final CompletableFuture<HttpResponse> response3 = new CompletableFuture<>();
        final Channel channel3 = new EmbeddedChannel();

        final HandleRegistry registry3 = new HandleRegistry(1, 0);
        final TimeoutHandle handle3 = mock(TimeoutHandle.class);
        final ChannelFuture headFuture3 = channel3.newFailedFuture(new IOException());
        final ChannelFuture dataFuture3 = channel3.newFailedFuture(new RuntimeException());
        registry3.put(new ResponseHandle(new HandleImpl(new NettyResponse(true)),
                request, ctx3, handle3, response3));

        transceiver.afterWriting(1, request, ctx3, FileWriter.singleton(),
                headFuture3, dataFuture3, handle3, registry3, response3);
        verify(handle3, never()).onWriteDone(any(), any());
        verify(handle3).onWriteFailed(any(), any(), any());
        verify(handle3).onError(any(), any(), any());
        assertTrue(response3.isCompletedExceptionally());
        assertTrue(Futures.getCause(response3) instanceof ConnectException);

        // case4: head success and data fails
        final ExecContext ctx4 = ExecContextUtil.from(new Context(), listener1);
        final CompletableFuture<HttpResponse> response4 = new CompletableFuture<>();
        final Channel channel4 = new EmbeddedChannel();

        final HandleRegistry registry4 = new HandleRegistry(1, 0);
        final TimeoutHandle handle4 = mock(TimeoutHandle.class);
        final ChannelFuture headFuture4 = channel4.newSucceededFuture();
        final ChannelFuture dataFuture4 = channel4.newFailedFuture(new IllegalStateException());
        registry4.put(new ResponseHandle(new HandleImpl(new NettyResponse(true)),
                request, ctx4, handle4, response4));

        transceiver.afterWriting(1, request, ctx4, FileWriter.singleton(),
                headFuture4, dataFuture4, handle4, registry4, response4);
        verify(handle4, never()).onWriteDone(any(), any());
        verify(handle4).onWriteFailed(any(), any(), any());
        verify(handle4).onError(any(), any(), any());
        assertTrue(response4.isCompletedExceptionally());
        assertTrue(Futures.getCause(response4) instanceof IOException);
    }

    @Test
    void testCompleteExceptionally() {
        final HttpTransceiverImpl transceiver = setUp();
        final HttpRequest request = mock(HttpRequest.class);
        final Listener listener = mock(Listener.class);
        final ExecContext ctx = ExecContextUtil.from(new Context(), listener);
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        transceiver.completeExceptionally(request, ctx, response, new IOException());
        assertTrue(response.isDone() && response.isCompletedExceptionally());
        verify(listener).onError(any(), any(), any());
        assertTrue(Futures.getCause(response) instanceof IOException);
    }

    @Test
    void testTryToCleanAndEndExceptionally() {
        final HttpTransceiverImpl transceiver = setUp();
        final HandleRegistry registry = new HandleRegistry(1, 0);

        // case1: registry == null and requestId == -1, which means has saved response handle.
        final TimeoutHandle handle1 = mock(TimeoutHandle.class);
        final CompletableFuture<HttpResponse> response1 = new CompletableFuture<>();
        final ResponseHandle rHandle1 = mock(ResponseHandle.class);
        registry.put(rHandle1);

        transceiver.tryToCleanAndEndExceptionally(mock(HttpRequest.class), ExecContextUtil.newAs(), 1,
                registry, handle1, response1, new IllegalStateException());
        verify(rHandle1).onError(any());
        assertNull(registry.get(1));

        registry.handleAndClearAll((h) -> {
        });
        clearInvocations(rHandle1);
        transceiver.tryToCleanAndEndExceptionally(mock(HttpRequest.class), ExecContextUtil.newAs(), 1,
                registry, handle1, response1, new IllegalStateException());
        verify(rHandle1, never()).onError(any());

        // case2: has completed before
        final TimeoutHandle handle2 = mock(TimeoutHandle.class);
        final CompletableFuture<HttpResponse> response2 = new CompletableFuture<>();
        response2.complete(mock(HttpResponse.class));
        transceiver.tryToCleanAndEndExceptionally(mock(HttpRequest.class), ExecContextUtil.newAs(), -1,
                registry, handle2, response2, new IllegalStateException());
        verify(handle2, never()).onError(any(), any(), any());
        assertFalse(response2.isCompletedExceptionally());

        // case3: complete exceptionally.
        final TimeoutHandle handle3 = mock(TimeoutHandle.class);
        final CompletableFuture<HttpResponse> response3 = new CompletableFuture<>();
        transceiver.tryToCleanAndEndExceptionally(mock(HttpRequest.class), ExecContextUtil.newAs(), -1,
                registry, handle3, response3, new IllegalStateException());
        verify(handle3).onError(any(), any(), any());
        assertTrue(response3.isDone() && response3.isCompletedExceptionally());
        assertTrue(Futures.getCause(response3) instanceof IllegalStateException);
    }

    @Test
    void testDetectOptions() {
        final int defaultReadTimeout = 6000;
        final int defaultConnectTimeout = 3000;
        final int defaultPoolSize = 512;
        final int defaultQueueSize = 256;

        final SocketAddress address1 = InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        final int connectTimeout = ThreadLocalRandom.current().nextInt(10, 10000);
        final int readTimeout = ThreadLocalRandom.current().nextInt(10, 10000);
        final int connectionPoolSize = ThreadLocalRandom.current().nextInt(1, 1000);
        final int connectionPoolWaitQueueSize = ThreadLocalRandom.current().nextInt(1, 1000);

        final ChannelPoolOptions options1 = ChannelPoolOptions.options()
                .readTimeout(readTimeout)
                .connectTimeout(connectTimeout)
                .poolSize(connectionPoolSize)
                .waitingQueueLength(connectionPoolWaitQueueSize)
                .build();

        final SocketAddress address2 = InetSocketAddress.createUnresolved("127.0.0.1", 9090);
        final ChannelPoolOptionsProvider provider = key -> {
            if (address1.equals(key)) {
                return options1;
            }
            return null;
        };

        // Case 1: ChannelPoolOptionsProvider is null
        final HttpClientBuilder builder = HttpClient.create();
        final HttpTransceiverImpl transceiver = new HttpTransceiverImpl(mock(EventLoopGroup.class),
                mock(CachedChannelPools.class), builder,
                ChannelPoolOptions.options()
                        .poolSize(builder.connectionPoolSize())
                        .connectTimeout(builder.connectTimeout())
                        .readTimeout(builder.readTimeout())
                        .waitingQueueLength(builder.connectionPoolWaitingQueueLength())
                        .build(),
                new ChannelPoolFactory(mock(SslEngineFactory.class)));

        final ChannelPoolOptions options11 = transceiver.detectOptions(address1);
        then(options11.readTimeout()).isEqualTo(defaultReadTimeout);
        then(options11.connectTimeout()).isEqualTo(defaultConnectTimeout);
        then(options11.poolSize()).isEqualTo(defaultPoolSize);
        then(options11.waitingQueueLength()).isEqualTo(defaultQueueSize);

        final ChannelPoolOptions options12 = transceiver.detectOptions(address2);
        then(options12.readTimeout()).isEqualTo(defaultReadTimeout);
        then(options12.connectTimeout()).isEqualTo(defaultConnectTimeout);
        then(options12.poolSize()).isEqualTo(defaultPoolSize);
        then(options12.waitingQueueLength()).isEqualTo(defaultQueueSize);

        // Case 2: using provider
        builder.channelPoolOptionsProvider(provider);
        final ChannelPoolOptions options21 = transceiver.detectOptions(address1);
        then(options21.readTimeout()).isEqualTo(readTimeout);
        then(options21.connectTimeout()).isEqualTo(connectTimeout);
        then(options21.poolSize()).isEqualTo(connectionPoolSize);
        then(options21.waitingQueueLength()).isEqualTo(connectionPoolWaitQueueSize);

        final ChannelPoolOptions options22 = transceiver.detectOptions(address2);
        then(options22.readTimeout()).isEqualTo(defaultReadTimeout);
        then(options22.connectTimeout()).isEqualTo(defaultConnectTimeout);
        then(options22.poolSize()).isEqualTo(defaultPoolSize);
        then(options22.waitingQueueLength()).isEqualTo(defaultQueueSize);

        // Case 3: default from HttpClientBuilder
        final HttpClientBuilder builder1 = HttpClient.create();
        builder1.readTimeout(1).connectTimeout(2).connectionPoolWaitingQueueLength(3).connectionPoolSize(4);
        final HttpTransceiverImpl transceiver1 = new HttpTransceiverImpl(mock(EventLoopGroup.class),
                mock(CachedChannelPools.class), builder1,
                ChannelPoolOptions.options()
                        .poolSize(builder1.connectionPoolSize())
                        .connectTimeout(builder1.connectTimeout())
                        .readTimeout(builder1.readTimeout())
                        .waitingQueueLength(builder1.connectionPoolWaitingQueueLength())
                        .build(),
                new ChannelPoolFactory(mock(SslEngineFactory.class)));
        final ChannelPoolOptions options3 = transceiver1.detectOptions(address1);
        then(options3.readTimeout()).isEqualTo(1);
        then(options3.connectTimeout()).isEqualTo(2);
        then(options3.poolSize()).isEqualTo(4);
        then(options3.waitingQueueLength()).isEqualTo(3);
    }

    private HttpTransceiverImpl setUp(CachedChannelPools channelPools) {
        EventLoopGroup ioThreads = mock(EventLoopGroup.class);
        HttpClientBuilder builder = HttpClient.create();
        ChannelPoolOptions options = ChannelPoolOptions.ofDefault();
        ChannelPoolFactory channelPoolFactory = new ChannelPoolFactory(mock(SslEngineFactory.class));
        return new HttpTransceiverImpl(ioThreads, channelPools, builder, options, channelPoolFactory);
    }

    private HttpTransceiverImpl setUp() {
        EventLoopGroup ioThreads = mock(EventLoopGroup.class);
        HttpClientBuilder builder = HttpClient.create();
        ChannelPoolOptions options = ChannelPoolOptions.ofDefault();
        ChannelPoolFactory channelPoolFactory = new ChannelPoolFactory(mock(SslEngineFactory.class));
        return new HttpTransceiverImpl(ioThreads, mock(CachedChannelPools.class), builder,
                options, channelPoolFactory);
    }
}
