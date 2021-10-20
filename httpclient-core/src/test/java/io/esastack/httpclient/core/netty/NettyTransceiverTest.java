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

import esa.commons.function.ThrowingSupplier;
import esa.commons.http.HttpVersion;
import esa.commons.netty.core.BufferImpl;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.HttpClientBuilder;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.Listener;
import io.esastack.httpclient.core.Scheme;
import io.esastack.httpclient.core.SegmentRequest;
import io.esastack.httpclient.core.config.ChannelPoolOptions;
import io.esastack.httpclient.core.config.SslOptions;
import io.esastack.httpclient.core.spi.SslEngineFactory;
import io.esastack.httpclient.core.util.Futures;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NettyTransceiverTest {

    private final HttpClient client = HttpClient.ofDefault();

    @Test
    void testConstructor() {
        final EventLoopGroup ioThreads = mock(EventLoopGroup.class);
        final ChannelPools channelPools = mock(ChannelPools.class);
        final HttpClientBuilder builder = HttpClient.create();
        final SslEngineFactory sslEngineFactory = mock(SslEngineFactory.class);

        assertThrows(NullPointerException.class, () -> new NettyTransceiver(null,
                channelPools, builder, sslEngineFactory));

        assertThrows(NullPointerException.class, () -> new NettyTransceiver(ioThreads,
                null, builder, sslEngineFactory));

        assertThrows(NullPointerException.class, () -> new NettyTransceiver(ioThreads,
                channelPools, null, sslEngineFactory));

        assertThrows(NullPointerException.class, () -> new NettyTransceiver(ioThreads,
                channelPools, builder, null));

        new NettyTransceiver(ioThreads, channelPools, builder, sslEngineFactory);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testHandle() {
        final EventLoopGroup ioThreads = mock(EventLoopGroup.class);
        final ChannelPools channelPools = mock(ChannelPools.class);
        final HttpClientBuilder builder = HttpClient.create();
        final SslEngineFactory sslEngineFactory = mock(SslEngineFactory.class);

        final NettyTransceiver transceiver = new NettyTransceiver(ioThreads,
                channelPools,
                builder,
                sslEngineFactory);

        final HttpRequest request = client.get("http://127.0.0.1:8080/abc").segment();
        final NettyContext ctx = new NettyContext();
        final Listener listener = mock(Listener.class);

        // Case 1: Error while acquiring channelPool
        when(channelPools.getIfPresent(any(SocketAddress.class))).thenThrow(new RuntimeException());

        final CompletableFuture<HttpResponse> response1 = transceiver.handle(request,
                ctx, null, listener);
        then(response1.isDone() && response1.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response1)).isInstanceOf(RuntimeException.class);
        verify(listener).onFiltersEnd(any(), any());
        verify(listener).onConnectionPoolAttempt(any(), any(), any());
        verify(listener).onAcquireConnectionPoolFailed(any(), any(), any(), any());

        final CompletableFuture<SegmentWriter> segmentWriterPromise1 = ctx.getWriter().orElse(Futures.completed());
        then(segmentWriterPromise1.isDone() && segmentWriterPromise1.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(segmentWriterPromise1)).isInstanceOf(RuntimeException.class);

        // Case 2: Acquired channelPool successfully.
        final io.netty.channel.pool.ChannelPool underlying = mock(io.netty.channel.pool.ChannelPool.class);
        final ChannelPool channelPool = new ChannelPool(underlying,
                ChannelPoolOptions.ofDefault(), false, () -> null);

        when(channelPools.getIfPresent(any(SocketAddress.class))).thenReturn(channelPool);

        // Acquire done and failure.
        final Future<Channel> future2 = mock(Future.class);
        when(underlying.acquire()).thenAnswer(answer -> future2);
        when(future2.isDone()).thenReturn(true);
        when(future2.isSuccess()).thenReturn(false);
        when(future2.cause()).thenReturn(new IllegalStateException());

        final NettyContext ctx1 = new NettyContext();
        final Listener listener2 = mock(Listener.class);
        final CompletableFuture<HttpResponse> response21 = transceiver
                .handle(request, ctx1, null, listener2);
        then(response21.isDone() && response21.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response21)).isInstanceOf(IOException.class);
        verify(listener2).onFiltersEnd(any(), any());
        verify(listener2).onConnectionPoolAttempt(any(), any(), any());
        verify(listener2).onConnectionPoolAcquired(any(), any(), any());
        verify(listener2).onConnectionAttempt(any(), any(), any());
        verify(listener2).onAcquireConnectionFailed(any(), any(), any(), any());
        verify(listener2).onError(any(), any(), any());

        final CompletableFuture<SegmentWriter> segmentWriterPromise2 = ctx1.getWriter().orElse(Futures.completed());
        then(segmentWriterPromise2.isDone() && segmentWriterPromise2.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(segmentWriterPromise2)).isInstanceOf(IOException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testHandle0() {
        final EventLoopGroup ioThreads = mock(EventLoopGroup.class);
        final ChannelPools channelPools = mock(ChannelPools.class);
        final HttpClientBuilder builder = HttpClient.create();
        final SslEngineFactory sslEngineFactory = mock(SslEngineFactory.class);

        final NettyTransceiver transceiver = new NettyTransceiver(ioThreads,
                channelPools,
                builder,
                sslEngineFactory);

        final HttpRequest request = client.get("http://127.0.0.1:8080/abc").segment();
        final Context ctx = new Context();
        final Listener listener = mock(Listener.class);
        final io.netty.channel.pool.ChannelPool channelPool = mock(io.netty.channel.pool.ChannelPool.class);
        final Future<Channel> future = mock(Future.class);
        final RequestWriter writer = NettyTransceiver.detectWriter(request);
        final CompletableFuture<HttpResponse> response1 = new CompletableFuture<>();
        final CompletableFuture<SegmentWriter> segmentWriterPromise1 = new CompletableFuture<>();

        // Case 1: unexpected error caught
        when(future.isSuccess()).thenReturn(true);
        final Channel channel = mock(Channel.class);
        when(future.getNow()).thenReturn(channel);
        when(channel.isActive()).thenReturn(false);
        transceiver.handle0(request, mock(SocketAddress.class), ctx, channelPool, future, null,
                listener, response1, writer, segmentWriterPromise1);
        verify(channelPool).release(any());
        verify(listener).onError(any(), any(), any());
        then(response1.isDone() && response1.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response1)).isInstanceOf(ConnectException.class);

        then(segmentWriterPromise1.isDone() && segmentWriterPromise1.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(segmentWriterPromise1)).isInstanceOf(ConnectException.class);
    }

    @Test
    void testDoWrite() throws Exception {
        final EventLoopGroup ioThreads = mock(EventLoopGroup.class);
        final ChannelPools channelPools = mock(ChannelPools.class);
        final HttpClientBuilder builder = HttpClient.create();
        final SslEngineFactory sslEngineFactory = mock(SslEngineFactory.class);

        final NettyTransceiver transceiver = new NettyTransceiver(ioThreads,
                channelPools,
                builder,
                sslEngineFactory);

        final HttpRequest request = client.get("http://127.0.0.1:8080/abc").segment();
        final Context ctx = new Context();
        final Listener listener = mock(Listener.class);
        final io.netty.channel.pool.ChannelPool channelPool = mock(io.netty.channel.pool.ChannelPool.class);
        final RequestWriter writer = NettyTransceiver.detectWriter(request);
        final CompletableFuture<HttpResponse> response1 = new CompletableFuture<>();
        final CompletableFuture<SegmentWriter> segmentWriterPromise1 = new CompletableFuture<>();

        final Channel channel = mock(Channel.class);
        final ChannelPipeline pipeline = mock(ChannelPipeline.class);
        when(channel.pipeline()).thenReturn(pipeline);
        when(pipeline.get(Http2ConnectionHandler.class)).thenReturn(mock(Http2ConnectionHandler.class));

        channel.pipeline().addLast(mock(Http2ConnectionHandler.class));

        // Case 1: channel inactive
        when(channel.isActive()).thenReturn(false);

        transceiver.doWrite(request, ctx, channelPool, channel, null,
                listener, response1, writer, segmentWriterPromise1);
        verify(channel).close();
        then(response1.isDone() && response1.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response1)).isInstanceOf(ConnectException.class);
        then(segmentWriterPromise1.isDone() && segmentWriterPromise1.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(segmentWriterPromise1)).isInstanceOf(ConnectException.class);
        verify(listener).onError(any(), any(), any());

        // Case 2: channel is not writable
        when(pipeline.get(Http2ConnectionHandler.class)).thenReturn(null);
        when(pipeline.get(Http1ChannelHandler.class)).thenReturn(mock(Http1ChannelHandler.class));
        when(channel.isActive()).thenReturn(true);
        when(channel.isWritable()).thenReturn(false);

        final Listener listener2 = mock(Listener.class);
        final CompletableFuture<HttpResponse> response2 = new CompletableFuture<>();
        final CompletableFuture<SegmentWriter> segmentWriterPromise2 = new CompletableFuture<>();

        transceiver.doWrite(request, ctx, channelPool, channel, null,
                listener2, response2, writer, segmentWriterPromise2);

        then(response2.isDone() && response2.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response2)).isSameAs(Utils.WRITE_BUF_IS_FULL);
        then(segmentWriterPromise2.isDone() && segmentWriterPromise2.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(segmentWriterPromise2)).isSameAs(Utils.WRITE_BUF_IS_FULL);
        verify(listener2).onError(any(), any(), any());


        // Case 3: Error while doWrite0
        when(channel.isWritable()).thenReturn(true);
        final Listener listener3 = mock(Listener.class);
        final CompletableFuture<HttpResponse> response3 = new CompletableFuture<>();
        final CompletableFuture<SegmentWriter> segmentWriterPromise3 = new CompletableFuture<>();

        transceiver.doWrite(request, ctx, channelPool, channel, null,
                listener3, response3, writer, segmentWriterPromise3);

        then(response3.isDone() && response3.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response3)).isInstanceOf(NullPointerException.class);
        then(segmentWriterPromise3.isDone() && segmentWriterPromise3.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(segmentWriterPromise3)).isInstanceOf(NullPointerException.class);
        verify(listener3).onError(any(), any(), any());
    }

    @Test
    void testDoWrite0() throws Exception {
        final EventLoopGroup ioThreads = mock(EventLoopGroup.class);
        final ChannelPools channelPools = mock(ChannelPools.class);
        final HttpClientBuilder builder = HttpClient.create();
        final SslEngineFactory sslEngineFactory = mock(SslEngineFactory.class);

        final NettyTransceiver transceiver = new NettyTransceiver(ioThreads,
                channelPools,
                builder,
                sslEngineFactory);

        final HttpRequest request = client.get("http://127.0.0.1:8080/abc").segment();
        final Context ctx = new Context();
        final TimeoutHandle h = mock(TimeoutHandle.class);
        final CompletableFuture<HttpResponse> response1 = new CompletableFuture<>();
        final CompletableFuture<SegmentWriter> segmentWriterPromise1 = new CompletableFuture<>();
        final Channel channel = new EmbeddedChannel();
        final HandleRegistry registry = new HandleRegistry(1, 0);
        channel.pipeline().addLast(new Http1ChannelHandler(registry, -1L));

        final ChannelFuture future = mock(ChannelFuture.class);
        final SegmentWriter writer = mock(SegmentWriter.class);
        when(writer.writeAndFlush(any(SegmentRequest.class),
                any(Channel.class),
                any(Context.class),
                any(ChannelPromise.class),
                anyBoolean(),
                any(io.netty.handler.codec.http.HttpVersion.class),
                anyBoolean())).thenReturn(future);

        // Case 1: write successfully
        when(future.isDone()).thenReturn(true);
        when(future.isSuccess()).thenReturn(true);

        transceiver.doWrite0(request, ctx,
                channel, (l, rsp) -> null, h,
                true, HttpVersion.HTTP_2, response1, writer, segmentWriterPromise1);
        verify(h).onWriteAttempt(any(), any());
        then(segmentWriterPromise1.isDone()).isTrue();
        then(segmentWriterPromise1.get()).isSameAs(writer);
        verify(h).onWriteDone(any(), any());

        // Case 2: write failure
        when(future.isSuccess()).thenReturn(false);
        when(future.cause()).thenReturn(new IllegalStateException());

        final CompletableFuture<HttpResponse> response2 = new CompletableFuture<>();
        final CompletableFuture<SegmentWriter> segmentWriterPromise2 = new CompletableFuture<>();

        transceiver.doWrite0(request, ctx, channel, (l, rsp) -> null, h,
                true, HttpVersion.HTTP_2, response2, writer, segmentWriterPromise2);
        verify(h).onWriteFailed(any(), any(), any());
        verify(h).onError(any(), any(), any());
        then(response2.isDone() && response2.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response2)).isInstanceOf(IOException.class);
        then(segmentWriterPromise2.isDone()).isTrue();
        then(segmentWriterPromise2.get()).isSameAs(writer);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetChannelPool() throws Throwable {
        final HttpClientBuilder builder = HttpClient.create();

        NettyHttpClientTest.NettyHttpClientImpl client = new NettyHttpClientTest.NettyHttpClientImpl(builder,
                true);
        final SslOptions sslOptions = SslOptions
                .options().handshakeTimeoutMillis(2000L).build();

        final SslEngineFactory factory = client.loadSslEngineFactory(sslOptions);

        final EventLoopGroup ioThreads = mock(EventLoopGroup.class);
        final ChannelPools channelPools = mock(ChannelPools.class);

        final NettyTransceiver transceiver = new NettyTransceiver(ioThreads, channelPools, builder, factory);
        final HttpRequest request = client.get("http://127.0.0.1:8080/abc");
        final SocketAddress address = InetSocketAddress.createUnresolved("127.0.0.1", 8080);

        final io.netty.channel.pool.ChannelPool underlying1 = mock(io.netty.channel.pool.ChannelPool.class);
        final ChannelPool channelPool1 = new ChannelPool(underlying1,
                ChannelPoolOptions.ofDefault(), true, () -> null);

        when(channelPools.getIfPresent(address)).thenReturn(channelPool1);
        final io.netty.channel.pool.ChannelPool pool1 = transceiver.getChannelPool(request, address);
        then(pool1).isSameAs(underlying1);

        when(channelPools.getIfPresent(address)).thenReturn(null);

        final AtomicBoolean ssl = new AtomicBoolean();
        final AtomicBoolean keepAlive = new AtomicBoolean();
        final AtomicReference<SocketAddress> address0 = new AtomicReference<>();
        final AtomicReference<EventLoopGroup> ioThreads0 = new AtomicReference<>();
        final AtomicReference<HttpClientBuilder> builder0 = new AtomicReference<>();
        final AtomicReference<ThrowingSupplier> supplier = new AtomicReference<>();

        when(channelPools.getOrCreate(anyBoolean(),
                anyBoolean(),
                any(SocketAddress.class),
                any(EventLoopGroup.class),
                any(HttpClientBuilder.class),
                any(ThrowingSupplier.class)))
                .thenAnswer(answer -> {
                    ssl.set(answer.getArgument(0));
                    keepAlive.set(answer.getArgument(1));
                    address0.set(answer.getArgument(2));
                    ioThreads0.set(answer.getArgument(3));
                    builder0.set(answer.getArgument(4));
                    supplier.set(answer.getArgument(5));
                    return channelPool1;
                });
        then(transceiver.getChannelPool(request, address)).isSameAs(pool1);
        then(ssl.get()).isSameAs(Scheme.HTTPS.name0().equals(request.scheme()));
        then(keepAlive.get()).isEqualTo(true);
        then(address0.get()).isEqualTo(address);
        then(ioThreads0.get()).isSameAs(ioThreads);
        then(builder0.get()).isNotSameAs(builder);
        supplier.get().get();

        // Handshake default to connectTimeout
        SslHandler sslHandler1 = (SslHandler) supplier.get().get();
        then(sslHandler1.getHandshakeTimeoutMillis()).isEqualTo(builder.connectTimeout() * 1000);

        // Handshake configured by sslOptions
        builder.connectTimeout(1);
        builder.sslOptions(sslOptions);
        when(channelPools.getOrCreate(anyBoolean(),
                anyBoolean(),
                any(SocketAddress.class),
                any(EventLoopGroup.class),
                any(HttpClientBuilder.class),
                any(ThrowingSupplier.class)))
                .thenAnswer(answer -> {
                    ssl.set(answer.getArgument(0));
                    keepAlive.set(answer.getArgument(1));
                    address0.set(answer.getArgument(2));
                    ioThreads0.set(answer.getArgument(3));
                    builder0.set(answer.getArgument(4));
                    supplier.set(answer.getArgument(5));
                    return channelPool1;
                });

        SslHandler sslHandler2 = (SslHandler) supplier.get().get();
        then(sslHandler2.getHandshakeTimeoutMillis()).isEqualTo(sslOptions.handshakeTimeoutMillis());
    }

    @Test
    void testDetectWriter() {
        final HttpRequest request1 = client.get("/abc");
        then(NettyTransceiver.detectWriter(request1)).isInstanceOf(PlainWriter.class);

        final HttpRequest request2 = client.post("/abc").body(new BufferImpl());
        then(NettyTransceiver.detectWriter(request2)).isInstanceOf(PlainWriter.class);

        final HttpRequest request3 = client.patch("/abc").body(new File(""));
        then(NettyTransceiver.detectWriter(request3)).isInstanceOf(FileWriter.class);

        final HttpRequest request4 = client.get("/abc").multipart().attr("", "");
        then(NettyTransceiver.detectWriter(request4)).isInstanceOf(MultipartWriter.class);

        final HttpRequest request5 = client.get("/abc").segment();
        then(NettyTransceiver.detectWriter(request5)).isInstanceOf(SegmentWriter.class);
    }
}
