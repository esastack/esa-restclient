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

import esa.commons.ExceptionUtils;
import esa.commons.function.ThrowingSupplier;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.config.Http1Options;
import esa.httpclient.core.config.Http2Options;
import esa.httpclient.core.util.HttpHeadersUtils;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DefaultHttp2ConnectionDecoder;
import io.netty.handler.codec.http2.DefaultHttp2ConnectionEncoder;
import io.netty.handler.codec.http2.DefaultHttp2FrameReader;
import io.netty.handler.codec.http2.DefaultHttp2FrameWriter;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.netty.handler.codec.http2.Http2ClientUpgradeCodec;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2FrameReader;
import io.netty.handler.codec.http2.Http2FrameWriter;
import io.netty.handler.codec.http2.Http2InboundFrameLogger;
import io.netty.handler.codec.http2.Http2OutboundFrameLogger;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;
import io.netty.util.internal.SystemPropertyUtil;

import java.net.ConnectException;

import static esa.httpclient.core.netty.ChannelPoolFactory.NETTY_CONFIGURE;

final class ChannelPoolHandler extends AbstractChannelPoolHandler {

    static final AttributeKey<ChannelFuture> HANDSHAKE_FUTURE = AttributeKey.valueOf("$handshake");

    private static final String INTERNAL_DEBUG_ENABLED_KEY = "esa.httpclient.internalDebugEnabled";

    private static final boolean INTERNAL_DEBUG_ENABLED = SystemPropertyUtil
            .getBoolean(INTERNAL_DEBUG_ENABLED_KEY, false);

    private final HttpClientBuilder builder;
    private final ThrowingSupplier<SslHandler> sslHandler;
    private final boolean ssl;

    ChannelPoolHandler(HttpClientBuilder builder,
                       ThrowingSupplier<SslHandler> sslHandler,
                       boolean ssl) {
        this.builder = builder;
        this.sslHandler = sslHandler;
        this.ssl = ssl;
    }

    @Override
    public void channelReleased(Channel ch) {
        ch.flush();
    }

    @Override
    public void channelCreated(Channel channel) {
        // Apply options
        applyOptions(channel);

        NETTY_CONFIGURE.onChannelCreated(channel);

        // TODO: try handshaking after channel has connected, see SimpleChannelPool.connectChannel()
        final ChannelPromise handshake = channel.newPromise();
        channel.attr(HANDSHAKE_FUTURE).set(handshake);

        // Apply handlers
        addHandlers(channel,
                builder.version(),
                builder.http1Options(),
                builder.http2Options(),
                ssl,
                builder.ish2ClearTextUpgrade(),
                builder.isUseDecompress(),
                handshake);

        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger().debug(channel + " has connected successfully");
            channel.closeFuture().addListener(f ->
                    LoggerUtils.logger().debug(channel + " has disconnected"));
        }
    }

    private void applyOptions(Channel channel) {
        int high = builder.netOptions() == null
                ? -1 : builder.netOptions().writeBufferHighWaterMark();
        int low = builder.netOptions() == null
                ? -1 : builder.netOptions().writeBufferLowWaterMark();
        if (high > 0) {
            if (low > 0) {
                channel.config().setWriteBufferWaterMark(new WriteBufferWaterMark(low, high));
            } else {
                channel.config().setWriteBufferHighWaterMark(high);
            }
        } else if (low > 0) {
            channel.config().setWriteBufferLowWaterMark(low);
        }
    }

    private void addHandlers(Channel channel,
                             esa.commons.http.HttpVersion version,
                             Http1Options http1Options,
                             Http2Options http2Options,
                             boolean ssl,
                             boolean h2ClearTextUpgrade,
                             boolean decompression,
                             ChannelPromise handshake) {
        final ChannelPipeline pipeline = channel.pipeline();

        if (INTERNAL_DEBUG_ENABLED) {
            pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
        }

        if (ssl) {
            final SslHandler sslHandler;
            try {
                sslHandler = this.sslHandler.get();
            } catch (Throwable ex) {
                throw new IllegalStateException("Failed to build SslHandler for https", ex);
            }

            if (sslHandler == null) {
                throw new IllegalStateException("SslHandler is null");
            }
            pipeline.addLast(sslHandler);

            // We must wait for the handshake to finish and the protocol to be negotiated before configuring
            // the HTTP/2 components of the pipeline.
            pipeline.addLast(new ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_1_1) {
                @Override
                protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
                    if (esa.commons.http.HttpVersion.HTTP_2 == version
                            && ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                        LoggerUtils.logger().info("Negotiated to use http2 successfully, connection: {}", channel);
                        addH2Handlers(ctx.pipeline(), http2Options, decompression);
                        handshake.setSuccess();
                    } else if (esa.commons.http.HttpVersion.HTTP_2 != (version) &&
                            ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
                        LoggerUtils.logger().info("Negotiated to use http1.1 successfully, connection: {}", channel);
                        addH1Handlers(ctx.pipeline(), http1Options, decompression);
                        handshake.setSuccess();
                    } else {
                        IllegalStateException ex = new IllegalStateException("Unexpected negotiated protocol: "
                                + protocol + ", configured: " + version);
                        handshake.setFailure(ex);
                        ctx.close();
                        throw ex;
                    }
                }

                @Override
                protected void handshakeFailure(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    handshake.setFailure(new ConnectException("Failed to handshake"));
                    super.handshakeFailure(ctx, cause);
                }
            });
        } else {
            if (esa.commons.http.HttpVersion.HTTP_2 == version) {
                if (h2ClearTextUpgrade) {
                    addH2cHandlers(pipeline,
                            http1Options,
                            http2Options,
                            decompression,
                            handshake);
                } else {
                    addH2Handlers(pipeline, http2Options, decompression);
                    handshake.setSuccess();
                }
            } else {
                addH1Handlers(pipeline, http1Options, decompression);
                handshake.setSuccess();
            }
        }
    }

    private void addH1Handlers(ChannelPipeline pipeline,
                               Http1Options http1Options,
                               boolean decompression) {
        HttpResponseDecoder decoder;
        if (http1Options == null) {
            decoder = new DelegatingHttpResponseDecoder();
        } else {
            decoder = new DelegatingHttpResponseDecoder(http1Options.maxInitialLineLength(),
                    http1Options.maxHeaderSize(),
                    http1Options.maxChunkSize());
        }
        pipeline.addLast(decoder);
        pipeline.addLast(new HttpRequestEncoder());
        if (decompression) {
            pipeline.addLast(new HttpContentDecompressor(false));
        }

        pipeline.addLast(new ChunkedWriteHandler());

        HandleRegistry registry = new HandleRegistry(1, 0);
        pipeline.addLast(new Http1ChannelHandler(registry,
                this.builder.maxContentLength()));
    }

    private void addH2Handlers(ChannelPipeline pipeline,
                               Http2Options http2Options,
                               boolean decompression) {
        final HandleRegistry registry = new HandleRegistry(2, 1);

        Http2ConnectionHandler h2Handler = buildH2Handler(registry, http2Options, decompression);
        pipeline.addLast(h2Handler);
    }

    private void addH2cHandlers(ChannelPipeline pipeline,
                                Http1Options http1Options,
                                Http2Options http2Options,
                                boolean decompression,
                                ChannelPromise handshake) {
        HandleRegistry registry = new HandleRegistry(2, 1);

        Http2ConnectionHandler h2Handler = buildH2Handler(registry, http2Options, decompression);

        HttpClientCodec codec = new HttpClientCodec();
        HttpClientUpgradeHandler upgrade = new HttpClientUpgradeHandler(codec,
                new UpgradeCodecImpl(h2Handler, h2Handler, handshake),
                65536);

        // Note: the codec handler must be removed before adding http1 handlers.
        pipeline.addLast("codec", codec);
        pipeline.addLast("upgrade", upgrade);
        pipeline.addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                ctx.writeAndFlush(buildH2cRequest());

                ctx.fireChannelActive();

                // Done with this handler, remove it from the pipeline.
                ctx.pipeline().remove(this);
            }
        });

        /*
         * 1. In common conditions, this handler is not reachable and will be removed
         * when upgrade to h2 successfully.
         *
         * 2. If it's failed to upgrade to h2, the handler will be triggered and http1
         *    handlers will be added and it will remove itself.
         */
        pipeline.addLast("fallbackToH1", new ChannelInboundHandlerAdapter() {

            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                if (HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_REJECTED == evt) {
                    ctx.pipeline().remove("codec");
                    ctx.pipeline().remove(this);
                    addH1Handlers(ctx.pipeline(), http1Options, decompression);
                    handshake.setSuccess();
                }
            }
        });
    }

    private Http2ConnectionHandler buildH2Handler(HandleRegistry registry,
                                                  Http2Options http2Options,
                                                  boolean decompression) {
        final Http2Connection connection = http2Options == null
                ? new DefaultHttp2Connection(false)
                : new DefaultHttp2Connection(false, http2Options.maxReservedStreams());

        Http2FrameWriter writer = new DefaultHttp2FrameWriter();
        Http2FrameReader reader = new DefaultHttp2FrameReader();
        if (http2Options != null) {
            try {
                ((DefaultHttp2FrameWriter) writer).maxFrameSize(http2Options.maxFrameSize());
                ((DefaultHttp2FrameReader) reader).maxFrameSize(http2Options.maxFrameSize());
            } catch (Http2Exception ex) {
                throw ExceptionUtils.asRuntime(ex);
            }
        }

        if (INTERNAL_DEBUG_ENABLED) {
            Http2FrameLogger frameLogger = new Http2FrameLogger(LogLevel.DEBUG);
            reader = new Http2InboundFrameLogger(reader, frameLogger);
            writer = new Http2OutboundFrameLogger(writer, frameLogger);
        }

        Http2ConnectionEncoder encoder = new DefaultHttp2ConnectionEncoder(connection, writer);
        Http2ConnectionDecoder decoder = new DefaultHttp2ConnectionDecoder(connection, encoder, reader);

        Http2ConnectionHandlerBuilder builder = new Http2ConnectionHandlerBuilder(registry).codec(decoder, encoder);

        builder.frameListener(decompression
                ? new DelegatingDecompressorFrameListener(connection,
                new Http2FrameHandler(registry, connection, this.builder.maxContentLength()))
                : new Http2FrameHandler(registry, connection, this.builder.maxContentLength()));
        if (http2Options != null) {
            builder.gracefulShutdownTimeoutMillis(http2Options.gracefulShutdownTimeoutMillis());
        }

        return builder.build();
    }

    private static DefaultHttpRequest buildH2cRequest() {
        return new DefaultFullHttpRequest(io.netty.handler.codec.http.HttpVersion.HTTP_1_1,
                HttpMethod.GET,
                "/",
                Unpooled.EMPTY_BUFFER);
    }

    private static final class UpgradeCodecImpl extends Http2ClientUpgradeCodec implements
            HttpClientUpgradeHandler.UpgradeCodec {

        private final Http2ConnectionHandler h2Handler;
        private final ChannelPromise handshake;

        private UpgradeCodecImpl(io.netty.handler.codec.http2.Http2ConnectionHandler connectionHandler,
                                 Http2ConnectionHandler h2Handler,
                                 ChannelPromise handshake) {
            super(connectionHandler);
            this.h2Handler = h2Handler;
            this.handshake = handshake;
        }

        @Override
        public void upgradeTo(ChannelHandlerContext ctx, FullHttpResponse upgradeResponse) {
            try {
                ctx.pipeline().remove("fallbackToH1");

                // Add the handler to the pipeline.
                ctx.pipeline().addLast(h2Handler);

                // Reserve local stream 1 for the response.
                h2Handler.onHttpClientUpgrade();
                handshake.setSuccess();
            } catch (Http2Exception e) {
                ctx.fireExceptionCaught(e);
                ctx.close();
            }
        }
    }

    static final class DelegatingHttpResponseDecoder extends HttpResponseDecoder {
        private DelegatingHttpResponseDecoder() {
        }

        private DelegatingHttpResponseDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
            super(maxInitialLineLength, maxHeaderSize, maxChunkSize);
        }

        @Override
        protected HttpMessage createMessage(String[] initialLine) {
            HttpMessage msg = super.createMessage(initialLine);
            msg.headers().add(HttpHeadersUtils.TTFB, System.currentTimeMillis());
            return msg;
        }
    }

}
