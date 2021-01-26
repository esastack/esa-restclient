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

import esa.commons.http.HttpVersion;
import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.config.Http2Options;
import esa.httpclient.core.config.NetOptions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.DecoratingHttp2ConnectionDecoder;
import io.netty.handler.codec.http2.DefaultHttp2ConnectionEncoder;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2FrameWriter;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import java.net.ConnectException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.netty.handler.codec.http.HttpResponseStatus.SWITCHING_PROTOCOLS;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http2.Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChannelInitializerTest {

    @Test
    void testApplyOptions() {
        final int writeBufferHighWaterMark = 1000;
        final int writeBufferLowWaterMark = 100;

        // Case 1: writeBufferHighWaterMark and writeBufferLowWaterMark
        final HttpClientBuilder builder1 = HttpClient.create()
                .netOptions(NetOptions
                        .options()
                        .writeBufferHighWaterMark(writeBufferHighWaterMark)
                        .writeBufferLowWaterMark(writeBufferLowWaterMark).build());
        final ChannelInitializer initializer1 = new ChannelInitializer(builder1, null, false);

        final Channel channel1 = new EmbeddedChannel();
        initializer1.onConnected(channel1.newSucceededFuture());
        then(channel1.config().getWriteBufferHighWaterMark()).isEqualTo(writeBufferHighWaterMark);
        then(channel1.config().getWriteBufferLowWaterMark()).isEqualTo(writeBufferLowWaterMark);

        // Case 2: writeBufferHighWaterMark only
        final HttpClientBuilder builder2 = HttpClient.create()
                .netOptions(NetOptions
                        .options()
                        .writeBufferHighWaterMark(WriteBufferWaterMark.DEFAULT.low() + 1).build());
        final ChannelInitializer initializer2 = new ChannelInitializer(builder2, null, false);
        final Channel channel2 = new EmbeddedChannel();
        initializer2.onConnected(channel2.newSucceededFuture());
        then(channel2.config().getWriteBufferHighWaterMark()).isEqualTo(WriteBufferWaterMark.DEFAULT.low() + 1);

        // Case 3: writeBufferLowWaterMark only
        final HttpClientBuilder builder3 = HttpClient.create()
                .netOptions(NetOptions
                        .options()
                        .writeBufferLowWaterMark(WriteBufferWaterMark.DEFAULT.high() - 1).build());
        final ChannelInitializer handler3 = new ChannelInitializer(builder3, null, false);
        final Channel initializer3 = new EmbeddedChannel();
        handler3.onConnected(initializer3.newSucceededFuture());
        then(initializer3.config().getWriteBufferLowWaterMark()).isEqualTo(WriteBufferWaterMark.DEFAULT.high() - 1);

        // Case 4: illegal arguments
        final HttpClientBuilder builder4 = HttpClient.create()
                .netOptions(NetOptions
                        .options()
                        .writeBufferHighWaterMark(writeBufferLowWaterMark)
                        .writeBufferLowWaterMark(writeBufferHighWaterMark).build());
        final ChannelInitializer initializer4 = new ChannelInitializer(builder4, null, false);
        final Channel channel4 = new EmbeddedChannel();
        assertThrows(IllegalArgumentException.class, () -> initializer4.onConnected(channel4.newSucceededFuture()));
    }

    @Test
    void testHttp10Directly() {
        testHttp11Directly();
    }

    @Test
    void testHttp11Directly() {
        // Case 1: decompressor is present
        final HttpClientBuilder builder1 = HttpClient.create().useDecompress(true);
        final ChannelInitializer initializer1 = new ChannelInitializer(builder1, null, false);
        final Channel channel1 = new EmbeddedChannel();
        final ChannelFuture connectFuture1 = initializer1.onConnected(channel1.newSucceededFuture());
        then(connectFuture1.isDone()).isTrue();
        then(connectFuture1.isSuccess()).isTrue();

        final ChannelPipeline pipeline1 = channel1.pipeline();
        then(pipeline1.get(HttpClientCodec.class)).isNotNull();
        then(pipeline1.get(HttpContentDecompressor.class)).isNotNull();
        then(pipeline1.get(ChunkedWriteHandler.class)).isNotNull();
        then(pipeline1.last()).isInstanceOf(Http1ChannelHandler.class);


        // Case 2: decompress is absent
        final HttpClientBuilder builder2 = HttpClient.create().useDecompress(true);
        final ChannelInitializer initializer2 = new ChannelInitializer(builder2, null, false);
        final Channel channel2 = new EmbeddedChannel();
        final ChannelFuture connectFuture2 = initializer2.onConnected(channel2.newSucceededFuture());
        then(connectFuture2.isDone()).isTrue();
        then(connectFuture2.isSuccess()).isTrue();

        final ChannelPipeline pipeline2 = channel2.pipeline();
        then(pipeline2.get(HttpClientCodec.class)).isNotNull();
        then(pipeline2.get(ChunkedWriteHandler.class)).isNotNull();
        then(pipeline2.last()).isInstanceOf(Http1ChannelHandler.class);
    }

    @Test
    void testH2Plain() {
        final boolean decompression = ThreadLocalRandom.current().nextBoolean();
        final long gracefulShutdownTimeoutMillis = ThreadLocalRandom.current().nextLong(1000);

        final HttpClientBuilder builder = HttpClient.create()
                .version(HttpVersion.HTTP_2)
                .h2ClearTextUpgrade(false)
                .useDecompress(decompression)
                .http2Options(Http2Options.options()
                        .gracefulShutdownTimeoutMillis(gracefulShutdownTimeoutMillis)
                        .build());

        final ChannelInitializer initializer = new ChannelInitializer(builder, null, false);
        final EmbeddedChannel channel = new EmbeddedChannel();
        final ChannelFuture connectFuture = initializer.onConnected(channel.newSucceededFuture());
        then(connectFuture.isDone()).isTrue();

        final ChannelPipeline pipeline = channel.pipeline();
        then(pipeline.get(SslHandler.class)).isNull();
        then(pipeline.get(Http2ConnectionHandler.class)).isNotNull();
        then(pipeline.get(Http1ChannelHandler.class)).isNull();

        validateHttp2Handler(pipeline.get(Http2ConnectionHandler.class),
                decompression, gracefulShutdownTimeoutMillis);
        channel.finishAndReleaseAll();
    }

    @Test
    void testH2CUpgradeSuccessfully() throws Exception {
        final boolean decompression = ThreadLocalRandom.current().nextBoolean();
        final long gracefulShutdownTimeoutMillis = ThreadLocalRandom.current().nextLong(1000);

        final HttpClientBuilder builder = HttpClient.create()
                .version(HttpVersion.HTTP_2)
                .h2ClearTextUpgrade(true)
                .useDecompress(decompression)
                .http2Options(Http2Options.options()
                        .gracefulShutdownTimeoutMillis(gracefulShutdownTimeoutMillis)
                        .build());

        final ChannelInitializer initializer = new ChannelInitializer(builder, () -> null, false);
        final EmbeddedChannel channel = new EmbeddedChannel();
        final ChannelFuture connectFuture = initializer.onConnected(channel.newSucceededFuture());
        then(connectFuture.isDone()).isFalse();
        final ChannelPipeline pipeline = channel.pipeline();
        then(pipeline.get(SslHandler.class)).isNull();

        then(pipeline.get(Http2ConnectionHandler.class)).isNull();
        then(pipeline.get(Http1ChannelHandler.class)).isNull();
        then(pipeline.get(HttpClientCodec.class)).isNotNull();
        then(pipeline.get(HttpClientUpgradeHandler.class)).isNotNull();
        then(pipeline.get(ApplicationProtocolNegotiationHandler.class)).isNull();
        then(connectFuture.isDone()).isFalse();

        pipeline.fireChannelActive();
        // Validate upgrade request has wrote
        validateH2cRequest(channel.readOutbound());

        final ChannelHandlerContext ctx = channel.pipeline().context(HttpClientUpgradeHandler.class);
        final HttpResponseEncoder0 encoder = new HttpResponseEncoder0();
        final ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        when(context.alloc()).thenReturn(ByteBufAllocator.DEFAULT);

        final List<Object> messages = new LinkedList<>();
        encoder.encode(ctx, createUpgradeResponse(HTTP_UPGRADE_PROTOCOL_NAME), messages);
        then(messages.size()).isEqualTo(1);

        channel.writeInbound(messages.get(0));
        then(connectFuture.isDone()).isTrue();
        then(connectFuture.isSuccess()).isTrue();

        validateHttp2Handler(channel.pipeline().get(Http2ConnectionHandler.class),
                decompression, gracefulShutdownTimeoutMillis);
        then(pipeline.get("fallbackToH1")).isNull();
        then(pipeline.get(HttpClientUpgradeHandler.class)).isNull();

        channel.finishAndReleaseAll();
    }

    @Test
    void testH2CUpgradeFails() throws Exception {
        final boolean decompression = ThreadLocalRandom.current().nextBoolean();

        final HttpClientBuilder builder = HttpClient.create()
                .version(HttpVersion.HTTP_2)
                .h2ClearTextUpgrade(true)
                .useDecompress(decompression);

        final ChannelInitializer initializer = new ChannelInitializer(builder, () -> null, false);
        final EmbeddedChannel channel = new EmbeddedChannel();
        final ChannelFuture connectFuture = initializer.onConnected(channel.newSucceededFuture());
        then(connectFuture.isDone()).isFalse();
        final ChannelPipeline pipeline = channel.pipeline();
        then(pipeline.get(SslHandler.class)).isNull();

        then(pipeline.get(Http2ConnectionHandler.class)).isNull();
        then(pipeline.get(Http1ChannelHandler.class)).isNull();
        then(pipeline.get(HttpClientCodec.class)).isNotNull();
        then(pipeline.get(HttpClientUpgradeHandler.class)).isNotNull();
        then(pipeline.get(ApplicationProtocolNegotiationHandler.class)).isNull();
        then(connectFuture.isDone()).isFalse();

        pipeline.fireChannelActive();
        // Validate upgrade request has wrote
        validateH2cRequest(channel.readOutbound());

        final ChannelHandlerContext ctx = channel.pipeline().context(HttpClientUpgradeHandler.class);
        final HttpResponseEncoder0 encoder = new HttpResponseEncoder0();
        final ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        when(context.alloc()).thenReturn(ByteBufAllocator.DEFAULT);

        final List<Object> messages = new LinkedList<>();
        encoder.encode(ctx, new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(200)), messages);
        then(messages.size()).isEqualTo(1);

        channel.writeInbound(messages.get(0));
        then(connectFuture.isDone()).isTrue();
        then(connectFuture.isSuccess()).isTrue();

        validateHttp1Handlers(channel.pipeline(), decompression);
        then(pipeline.get("fallbackToH1")).isNull();
        then(pipeline.get(HttpClientUpgradeHandler.class)).isNull();

        channel.finishAndReleaseAll();
    }

    @Test
    void testHttp11s() throws Exception {
        final boolean decompression = ThreadLocalRandom.current().nextBoolean();
        final boolean isHttp10 = ThreadLocalRandom.current().nextBoolean();

        final HttpClientBuilder builder = HttpClient.create()
                .version(isHttp10 ? HttpVersion.HTTP_1_0 : HttpVersion.HTTP_1_1)
                .useDecompress(decompression);

        final SslHandler sslHandler = mock(SslHandler.class);
        final ChannelInitializer initializer = new ChannelInitializer(builder, () -> sslHandler, true);
        final EmbeddedChannel channel = new EmbeddedChannel();
        final ChannelFuture connectFuture = initializer.onConnected(channel.newSucceededFuture());
        then(connectFuture.isDone()).isFalse();
        final ChannelPipeline pipeline = channel.pipeline();
        then(pipeline.get(SslHandler.class)).isNotNull();
        final ApplicationProtocolNegotiationHandler negotiation = pipeline
                .get(ApplicationProtocolNegotiationHandler.class);
        then(negotiation).isNotNull();
        final ChannelHandlerContext context = pipeline.context(ApplicationProtocolNegotiationHandler.class);
        when(sslHandler.applicationProtocol()).thenReturn("http/1.1");

        negotiation.userEventTriggered(context, SslHandshakeCompletionEvent.SUCCESS);

        then(pipeline.get(Http2ConnectionHandler.class)).isNull();
        then(pipeline.get(Http1ChannelHandler.class)).isNotNull();
        then(pipeline.get(ApplicationProtocolNegotiationHandler.class)).isNull();
        then(connectFuture.isSuccess()).isTrue();

        validateHttp1Handlers(channel.pipeline(), decompression);

        channel.finishAndReleaseAll();
    }

    @Test
    void testHttp2s() throws Exception {
        final boolean decompression = ThreadLocalRandom.current().nextBoolean();
        final long gracefulShutdownTimeoutMillis = ThreadLocalRandom.current().nextLong(1000);

        final HttpClientBuilder builder = HttpClient.create()
                .version(HttpVersion.HTTP_2)
                .useDecompress(decompression)
                .http2Options(Http2Options.options()
                        .gracefulShutdownTimeoutMillis(gracefulShutdownTimeoutMillis)
                        .build());

        final SslHandler sslHandler = mock(SslHandler.class);
        final ChannelInitializer initializer = new ChannelInitializer(builder, () -> sslHandler, true);
        final EmbeddedChannel channel = new EmbeddedChannel();
        final ChannelFuture connectFuture = initializer.onConnected(channel.newSucceededFuture());
        then(connectFuture.isDone()).isFalse();
        final ChannelPipeline pipeline = channel.pipeline();
        then(pipeline.get(SslHandler.class)).isNotNull();
        final ApplicationProtocolNegotiationHandler negotiation = pipeline
                .get(ApplicationProtocolNegotiationHandler.class);
        then(negotiation).isNotNull();
        final ChannelHandlerContext context = pipeline.context(ApplicationProtocolNegotiationHandler.class);
        when(sslHandler.applicationProtocol()).thenReturn("h2");

        negotiation.userEventTriggered(context, SslHandshakeCompletionEvent.SUCCESS);

        then(pipeline.get(Http2ConnectionHandler.class)).isNotNull();
        then(pipeline.get(Http1ChannelHandler.class)).isNull();
        then(pipeline.get(ApplicationProtocolNegotiationHandler.class)).isNull();
        then(connectFuture.isSuccess()).isTrue();

        validateHttp2Handler(pipeline.get(Http2ConnectionHandler.class),
                decompression, gracefulShutdownTimeoutMillis);

        channel.finishAndReleaseAll();
    }

    @Test
    void testNegotiateFails() throws Exception {
        final HttpClientBuilder builder = HttpClient.create();

        // Illegal Argument
        assertThrows(IllegalStateException.class, () ->
                new ChannelInitializer(builder, null, true)
                        .onConnected(new EmbeddedChannel().newSucceededFuture()));

        assertThrows(IllegalStateException.class, () ->
                new ChannelInitializer(builder, () -> null, true)
                        .onConnected(new EmbeddedChannel().newSucceededFuture()));

        final SslHandler sslHandler = mock(SslHandler.class);
        final ChannelInitializer initializer = new ChannelInitializer(builder, () -> sslHandler, true);
        final EmbeddedChannel channel = new EmbeddedChannel();

        // Case 1: failed to connect
        final ChannelFuture connectFuture1 = initializer
                .onConnected(channel.newFailedFuture(new ConnectException()));
        then(connectFuture1.isDone()).isTrue();
        then(connectFuture1.isSuccess()).isFalse();
        then(connectFuture1.cause()).isInstanceOf(ConnectException.class);

        // Case 2: handshake fails. and channel will close
        final ChannelFuture connectFuture2 = initializer.onConnected(channel.newSucceededFuture());
        then(connectFuture2.isDone()).isFalse();
        final ChannelPipeline pipeline = channel.pipeline();

        then(pipeline.get(SslHandler.class)).isNotNull();
        final ApplicationProtocolNegotiationHandler negotiation = pipeline
                .get(ApplicationProtocolNegotiationHandler.class);
        then(negotiation).isNotNull();

        final ChannelHandlerContext context = pipeline.context(ApplicationProtocolNegotiationHandler.class);
        negotiation.exceptionCaught(context, new DecoderException(new SSLException("")));
        then(connectFuture2.isDone()).isTrue();
        then(connectFuture2.cause()).isInstanceOf(ConnectException.class);

        channel.finishAndReleaseAll();
    }

    private void validateHttp1Handlers(ChannelPipeline pipeline, boolean decompression) {
        then(pipeline.get(HttpClientCodec.class)).isNotNull();
        then(pipeline.get(ChunkedWriteHandler.class)).isNotNull();
        then(pipeline.get(Http1ChannelHandler.class)).isNotNull();
        if (decompression) {
            then(pipeline.get(HttpContentDecompressor.class)).isNotNull();
        } else {
            then(pipeline.get(HttpContentDecompressor.class)).isNull();
        }
    }

    private void validateHttp2Handler(Http2ConnectionHandler target,
                                      boolean decompression,
                                      long gracefulShutdownTimeoutMillis) {
        final Http2ConnectionEncoder encoder = target.encoder();
        final Http2ConnectionDecoder decoder = target.decoder();

        then(encoder).isInstanceOf(DefaultHttp2ConnectionEncoder.class);
        then(decoder).isInstanceOf(DecoratingHttp2ConnectionDecoder.class);

        then(encoder.frameWriter()).isInstanceOf(Http2FrameWriter.class);
        if (decompression) {
            then(target.decoder().frameListener()).isInstanceOf(DelegatingDecompressorFrameListener.class);
        } else {
            then(target.decoder().frameListener()).isInstanceOf(Http2FrameHandler.class);
        }

        then(target.gracefulShutdownTimeoutMillis()).isEqualTo(gracefulShutdownTimeoutMillis);
    }

    private void validateH2cRequest(ByteBuf data) throws Exception {
        HttpRequestDecoder0 decoder = new HttpRequestDecoder0();
        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        when(context.alloc()).thenReturn(ByteBufAllocator.DEFAULT);
        final List<Object> out = new LinkedList<>();
        decoder.decode(context, data, out);
        then(out.size()).isEqualTo(2);

        final HttpRequest request = (HttpRequest) out.get(0);
        then(request.headers().get(HttpHeaderNames.UPGRADE)).isEqualTo(HTTP_UPGRADE_PROTOCOL_NAME);

        then(out.get(1)).isInstanceOf(LastHttpContent.class);
    }

    private static FullHttpResponse createUpgradeResponse(CharSequence upgradeProtocol) {
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, SWITCHING_PROTOCOLS,
                Unpooled.EMPTY_BUFFER, false);
        res.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE);
        res.headers().add(HttpHeaderNames.UPGRADE, upgradeProtocol);
        return res;
    }

    private static class HttpResponseEncoder0 extends HttpResponseEncoder {
        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
            super.encode(ctx, msg, out);
        }
    }

    private static class HttpRequestDecoder0 extends HttpRequestDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
            super.decode(ctx, buffer, out);
        }
    }

}

