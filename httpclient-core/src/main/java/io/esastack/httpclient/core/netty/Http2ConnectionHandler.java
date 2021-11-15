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
import io.esastack.httpclient.core.util.BufferUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.stream.ChunkedWriteHandler;

import static io.esastack.httpclient.core.netty.Utils.handleIdleEvt;
import static io.netty.buffer.ByteBufUtil.writeAscii;
import static io.netty.handler.codec.http2.Http2CodecUtil.getEmbeddedHttp2Exception;
import static io.netty.handler.codec.http2.Http2Error.NO_ERROR;

class Http2ConnectionHandler extends io.netty.handler.codec.http2.Http2ConnectionHandler {

    private final HandleRegistry registry;
    private volatile ChannelHandlerContext ctx;

    Http2ConnectionHandler(Http2ConnectionDecoder decoder,
                           Http2ConnectionEncoder encoder,
                           Http2Settings initialSettings,
                           boolean decoupleCloseAndGoAway,
                           HandleRegistry registry) {
        super(decoder, encoder, initialSettings, decoupleCloseAndGoAway);
        this.registry = registry;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;

        // add a ChunkedWriteHandler after Http2ConnectionChunkHandler
        // usually we use the Http2ConnectionChunkHandler#encoder() to write http2 response, and this
        // ChunkedWriteHandler we added here is used to handle the messages type of Http2ChunkedInput(eg. large file)
        ctx.pipeline().addAfter(ctx.name(), "h2ChunkedWriter", new ChunkedWriteHandler());
        super.handlerAdded(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Http2ChunkedInput.Content) {
            Http2ChunkedInput.Content c = (Http2ChunkedInput.Content) msg;
            boolean hasBody = c.content().readableBytes() > 0;

            if (hasBody) {
                writeData(c.streamId,
                        c.content(),
                        c.endOfStream,
                        promise);
            } else {
                writeData(c.streamId,
                        Unpooled.EMPTY_BUFFER,
                        true,
                        promise);
            }
        } else {
            super.write(ctx, msg, promise);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        try {
            if (getEmbeddedHttp2Exception(cause) != null) {
                super.exceptionCaught(ctx, cause);
            } else {
                Utils.handleH2ChannelEx(registry, ctx.channel(), cause);
            }
        } finally {
            ctx.close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!handleIdleEvt(ctx, evt)) {
            super.userEventTriggered(ctx, evt);
        }
    }

    ChannelFuture writeData(int streamId,
                            Object data,
                            boolean endStream,
                            ChannelPromise promise) {
        if (inEventLoop()) {
            return writeData0(streamId, data, endStream, promise);
        } else {
            final ChannelPromise promise0 = ctx.newPromise();
            final Runnable runnable = () -> writeData0(streamId,
                    data,
                    endStream,
                    promise)
                    .addListener(future -> {
                        if (future.isSuccess()) {
                            promise0.setSuccess();
                        } else {
                            promise0.setFailure(future.cause());
                        }
                    });

            ctx.channel().eventLoop().execute(runnable);
            return promise0;
        }
    }

    private ChannelFuture writeData0(int streamId,
                                     Object data,
                                     boolean endStream,
                                     ChannelPromise promise) {
        if (checkIfEnded(streamId, false, promise)) {
            return promise;
        }

        ByteBuf buf = null;
        try {
            if (data != null) {
                buf = format(data);
            }

            boolean emptyData = buf == null || buf.readableBytes() == 0;
            if (emptyData && !endStream) {
                return promise.setSuccess();
            }

            return encoder().writeData(ctx,
                    streamId,
                    buf,
                    0,
                    endStream,
                    promise);
        } catch (Throwable ex) {
            Utils.tryRelease(buf);
            return promise.setFailure(ex);
        }
    }

    ChannelFuture writeHeaders(int streamId,
                               Http2Headers headers,
                               boolean endStream,
                               ChannelPromise promise) {
        if (inEventLoop()) {
            return writeHeaders0(streamId, headers, endStream, promise);
        } else {
            final ChannelPromise promise0 = ctx.newPromise();
            final Runnable runnable = () -> writeHeaders0(streamId,
                    headers,
                    endStream,
                    promise)
                    .addListener(future -> {
                        if (future.isSuccess()) {
                            promise0.setSuccess();
                        } else {
                            promise0.setFailure(future.cause());
                        }
                    });

            ctx.channel().eventLoop().execute(runnable);
            return promise0;
        }
    }

    ChannelFuture writeGoAwayOnExhaustion(ChannelPromise promise) {
        final ChannelPromise promise0 = ctx.newPromise();

        final Runnable runnable = () -> encoder().writeGoAway(ctx, Integer.MAX_VALUE - 1,
                NO_ERROR.code(),
                writeAscii(ctx.alloc(),
                        "Stream IDs exhausted on local stream creation"), promise)
                .addListener(future -> {
                    if (future.isSuccess()) {
                        promise0.setSuccess();
                    } else {
                        promise0.setFailure(future.cause());
                    }
                });

        if (inEventLoop()) {
            runnable.run();
        } else {
            ctx.channel().eventLoop().execute(runnable);
        }

        return promise0;
    }

    private ChannelFuture writeHeaders0(int streamId,
                                        Http2Headers headers,
                                        boolean endStream,
                                        ChannelPromise promise) {
        if (checkIfEnded(streamId, true, promise)) {
            return promise;
        }

        int dependencyId = headers.getInt(
                HttpConversionUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(), 0);
        short weight = headers.getShort(
                HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(),
                Http2CodecUtil.DEFAULT_PRIORITY_WEIGHT);

        return encoder().writeHeaders(ctx,
                streamId,
                headers,
                dependencyId,
                weight,
                false,
                0,
                endStream,
                promise);
    }

    boolean checkIfEnded(int streamId, boolean isHeader, ChannelPromise promise) {
        if (registry.get(streamId) == null) {
            promise.setFailure(new IllegalStateException("Request may has ended before writing "
                    + (isHeader ? "headers" : "data")));
            return true;
        }

        return false;
    }

    HandleRegistry getRegistry() {
        return registry;
    }

    private boolean inEventLoop() {
        return ctx.channel().eventLoop().inEventLoop();
    }

    private ByteBuf format(Object data) {
        if (data instanceof ByteBuf) {
            return (ByteBuf) data;
        } else if (data instanceof byte[]) {
            ByteBuf buf = ctx.alloc().buffer(((byte[]) data).length);
            buf.writeBytes((byte[]) data);
            return buf;
        } else if (data instanceof Buffer) {
            return BufferUtils.toByteBuf(((Buffer) data));
        } else {
            throw new IllegalArgumentException("Unsupported writable data format: " + data.getClass()
                    + "(expected ByteBuf, Buffer, byte[])");
        }
    }

}
