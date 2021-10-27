/*
 * Copyright 2021 OPPO ESA Stack Project
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

import io.esastack.httpclient.core.ExecContextUtil;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.HttpClientBuilder;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpUri;
import io.esastack.httpclient.core.config.ChannelPoolOptions;
import io.esastack.httpclient.core.spi.SslEngineFactory;
import io.esastack.httpclient.core.util.Futures;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NettyTransceiverTest {

    @Test
    void testHandle() {
        final NettyTransceiver transceiver = setUp();
        final HttpRequest request = HttpClient.ofDefault().get("http://127.0.0.1:8080").segment();
        final NettyExecContext ctx = ExecContextUtil.newAsNetty();

        transceiver.handle(request, ctx);
        assertTrue(ctx.segmentWriter().isPresent());
    }

    @Test
    void testOnAcquireChannelPoolFailure() {
        final NettyTransceiver transceiver = setUp();
        final HttpRequest request = mock(HttpRequest.class);
        when(request.isSegmented()).thenReturn(true);
        final NettyExecContext ctx = ExecContextUtil.newAsNetty();
        ctx.segmentWriter(new CompletableFuture<>());

        final IOException cause = new IOException();
        transceiver.onAcquireChannelPoolFailure(request, ctx, mock(SocketAddress.class), cause);
        assertTrue(ctx.segmentWriter().isPresent());
        assertSame(cause, Futures.getCause(ctx.segmentWriter().get()));
    }

    @Test
    void testOnAcquireChannelFailure() {
        final NettyTransceiver transceiver = setUp();
        final HttpRequest request = mock(HttpRequest.class);
        when(request.isSegmented()).thenReturn(true);
        final NettyExecContext ctx = ExecContextUtil.newAsNetty();
        ctx.segmentWriter(new CompletableFuture<>());

        final IOException cause = new IOException();
        transceiver.onAcquireChannelFailure(request, mock(SocketAddress.class), ctx,
                new CompletableFuture<>(), cause);
        assertTrue(ctx.segmentWriter().isPresent());
        assertSame(cause, Futures.getCause(ctx.segmentWriter().get()));
    }

    @Test
    void testCompleteExceptionally() {
        final NettyTransceiver transceiver = setUp();
        final HttpRequest request = mock(HttpRequest.class);
        when(request.isSegmented()).thenReturn(true);
        final NettyExecContext ctx = ExecContextUtil.newAsNetty();
        ctx.segmentWriter(new CompletableFuture<>());

        final IOException cause = new IOException();
        transceiver.completeExceptionally(request, ctx, new CompletableFuture<>(), cause);
        assertTrue(ctx.segmentWriter().isPresent());
        assertSame(cause, Futures.getCause(ctx.segmentWriter().get()));
    }

    @Test
    void testAfterWriting() throws Throwable {
        final NettyTransceiver transceiver = setUp();
        final HttpRequest request = mock(HttpRequest.class);
        when(request.uri()).thenReturn(new HttpUri("/abc"));
        when(request.isSegmented()).thenReturn(true);
        final NettyExecContext ctx = ExecContextUtil.newAsNetty();
        final Channel channel = new EmbeddedChannel();
        ctx.segmentWriter(new CompletableFuture<>());
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final RequestWriter writer = new SegmentWriter();

        transceiver.afterWriting(-1, request, ctx, writer, channel.newSucceededFuture(),
                channel.newSucceededFuture(), mock(TimeoutHandle.class), registry,
                new CompletableFuture<>());
        assertTrue(ctx.segmentWriter().isPresent());
        assertSame(writer, ctx.segmentWriter().get().get());
    }

    @Test
    void testTryToCleanAndEndExceptionally() {
        final NettyTransceiver transceiver = setUp();
        final HttpRequest request = mock(HttpRequest.class);
        when(request.uri()).thenReturn(new HttpUri("/abc"));
        when(request.isSegmented()).thenReturn(true);
        final NettyExecContext ctx = ExecContextUtil.newAsNetty();
        final IOException cause = new IOException();
        ctx.segmentWriter(new CompletableFuture<>());
        final HandleRegistry registry = new HandleRegistry(1, 0);

        transceiver.tryToCleanAndEndExceptionally(request, ctx, -1, registry, mock(TimeoutHandle.class),
                new CompletableFuture<>(), cause);
        assertTrue(ctx.segmentWriter().isPresent());
        assertSame(cause, Futures.getCause(ctx.segmentWriter().get()));
    }

    private NettyTransceiver setUp() {
        EventLoopGroup ioThreads = mock(EventLoopGroup.class);
        HttpClientBuilder builder = HttpClient.create();
        ChannelPoolOptions options = ChannelPoolOptions.ofDefault();
        ChannelPoolFactory channelPoolFactory = new ChannelPoolFactory(mock(SslEngineFactory.class));
        return new NettyTransceiver(ioThreads, mock(CachedChannelPools.class), builder,
                options, channelPoolFactory);
    }
}

