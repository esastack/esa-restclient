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
import esa.commons.ExceptionUtils;
import io.esastack.httpclient.core.HttpClientBuilder;
import io.esastack.httpclient.core.config.Http1Options;
import io.esastack.httpclient.core.config.Http2Options;
import io.esastack.httpclient.core.util.LoggerUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
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
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.SystemPropertyUtil;

import java.net.ConnectException;

import static io.esastack.httpclient.core.netty.ChannelPoolFactory.NETTY_CONFIGURE;

/**
 * Initializes the {@link Channel} as soon as {@link Bootstrap#connect()} completed, And we can negotiate
 * the {@link HttpVersion} with remote peer and then add {@link ChannelHandler}s.
 */
final class ChannelInitializer {

    private static final String INTERNAL_DEBUG_ENABLED_KEY = "io.esastack.httpclient.internalDebugEnabled";

    private static final boolean INTERNAL_DEBUG_ENABLED = SystemPropertyUtil
            .getBoolean(INTERNAL_DEBUG_ENABLED_KEY, false);

    private final boolean ssl;
    private final SslHandler sslHandler;
    private final HttpClientBuilder builder;

    ChannelInitializer(boolean ssl, SslHandler sslHandler, HttpClientBuilder builder) {
        Checks.checkNotNull(builder, "builder");
        this.ssl = ssl;
        this.sslHandler = sslHandler;
        this.builder = builder;
    }

    ChannelFuture onConnected(ChannelFuture connectFuture) {
        if (connectFuture.isDone() && !connectFuture.isSuccess()) {
            return connectFuture;
        }

        final Channel channel = connectFuture.channel();
        final ChannelPromise initializeFuture = channel.newPromise();
        if (connectFuture.isDone()) {
            if (connectFuture.isSuccess()) {
                doInitialize(channel, initializeFuture);
                return initializeFuture;
            } else {
                return connectFuture;
            }
        } else {
            connectFuture.addListener(future -> {
                if (future.isSuccess()) {
                    doInitialize(channel, initializeFuture);
                } else {
                    initializeFuture.setFailure(future.cause());
                }
            });

            return initializeFuture;
        }
    }

    private void doInitialize(Channel channel, ChannelPromise initializeFuture) {
        // Apply options
        applyOptions(channel);

        NETTY_CONFIGURE.onChannelCreated(channel);

        // Apply handlers
        addHandlers(channel,
                builder.version(),
                builder.http1Options(),
                builder.http2Options(),
                ssl,
                builder.isH2ClearTextUpgrade(),
                builder.isUseDecompress(),
                initializeFuture);

        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger().debug("Connection: " + channel + " has connected successfully.");
            channel.closeFuture().addListener(f ->
                    LoggerUtils.logger().debug("Connection: " + channel + " has closed."));
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
                             ChannelPromise initializeFuture) {
        final ChannelPipeline pipeline = channel.pipeline();

        if (INTERNAL_DEBUG_ENABLED) {
            pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
        }

        if (builder.idleTimeoutSeconds() > 0) {
            pipeline.addLast(new IdleStateHandler(0,
                    0, builder.idleTimeoutSeconds()));
        }

        if (ssl) {
            if (sslHandler == null) {
                throw new IllegalStateException("SslHandler is absent");
            }
            pipeline.addLast(sslHandler);

            // We must wait for the initialization to finish and the protocol to be negotiated before configuring
            // the HTTP/2 components of the pipeline.
            pipeline.addLast(new ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_1_1) {
                @Override
                protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
                    if (esa.commons.http.HttpVersion.HTTP_2 == version
                            && ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                        if (LoggerUtils.logger().isDebugEnabled()) {
                            LoggerUtils.logger().debug("Negotiated to use http2 successfully, connection: {}",
                                    channel);
                        }
                        addH2Handlers(ctx.pipeline(), http2Options, decompression);
                        initializeFuture.setSuccess();
                    } else if (esa.commons.http.HttpVersion.HTTP_2 != (version) &&
                            ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
                        if (LoggerUtils.logger().isDebugEnabled()) {
                            LoggerUtils.logger().debug("Negotiated to use http1.1 successfully, connection: {}",
                                    channel);
                        }
                        addH1Handlers(ctx.pipeline(), http1Options, decompression);
                        initializeFuture.setSuccess();
                    } else {
                        IllegalStateException ex = new IllegalStateException("Unexpected negotiated protocol: "
                                + protocol + ", configured: " + version);
                        initializeFuture.setFailure(ex);
                        ctx.close();
                        throw ex;
                    }
                }

                @Override
                protected void handshakeFailure(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    initializeFuture.setFailure(new ConnectException("Failed to handshake"));
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
                            initializeFuture);
                } else {
                    addH2Handlers(pipeline, http2Options, decompression);
                    initializeFuture.setSuccess();
                }
            } else {
                addH1Handlers(pipeline, http1Options, decompression);
                initializeFuture.setSuccess();
            }
        }
    }

    private void addH1Handlers(ChannelPipeline pipeline,
                               Http1Options http1Options,
                               boolean decompression) {
        final HttpClientCodec codec;
        if (http1Options == null) {
            codec = new HttpClientCodec();
        } else {
            codec = new HttpClientCodec(http1Options.maxInitialLineLength(),
                    http1Options.maxHeaderSize(),
                    http1Options.maxChunkSize());
        }
        pipeline.addLast(codec);
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
                                ChannelPromise initializeFuture) {
        HandleRegistry registry = new HandleRegistry(2, 1);

        Http2ConnectionHandler h2Handler = buildH2Handler(registry, http2Options, decompression);

        HttpClientCodec codec = new HttpClientCodec();
        HttpClientUpgradeHandler upgrade = new HttpClientUpgradeHandler(codec,
                new UpgradeCodecImpl(h2Handler, h2Handler, initializeFuture),
                65536);

        // Note: the codec handler must be removed before adding http1 handlers.
        pipeline.addLast("codec", codec);
        pipeline.addLast("upgrade", upgrade);
        pipeline.addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                ctx.writeAndFlush(buildH2cRequest());

                ctx.fireChannelActive();

                // Remove itself from the pipeline after writing first request.
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
                    initializeFuture.setSuccess();
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
        private final ChannelPromise initializeFuture;

        private UpgradeCodecImpl(io.netty.handler.codec.http2.Http2ConnectionHandler connectionHandler,
                                 Http2ConnectionHandler h2Handler,
                                 ChannelPromise initializeFuture) {
            super(connectionHandler);
            this.h2Handler = h2Handler;
            this.initializeFuture = initializeFuture;
        }

        @Override
        public void upgradeTo(ChannelHandlerContext ctx, FullHttpResponse upgradeResponse) {
            try {
                ctx.pipeline().remove("fallbackToH1");

                // Add the handler to the pipeline.
                ctx.pipeline().addLast(h2Handler);

                // Reserve local stream 1 for the response.
                h2Handler.onHttpClientUpgrade();
                initializeFuture.setSuccess();
            } catch (Http2Exception e) {
                ctx.fireExceptionCaught(e);
                ctx.close();
            }
        }
    }

}
