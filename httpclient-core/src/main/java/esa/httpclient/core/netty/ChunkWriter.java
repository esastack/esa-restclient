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

import esa.commons.StringUtils;
import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpHeaders;
import esa.commons.netty.core.Buffer;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.httpclient.core.ChunkRequest;
import esa.httpclient.core.Context;
import esa.httpclient.core.util.HttpHeadersUtils;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;

import java.io.IOException;

import static esa.httpclient.core.util.HttpHeadersUtils.toHttp2Headers;

/**
 * This class is designed as thread-safe, so we need't worry about a serial of conflicts.
 */
class ChunkWriter extends RequestWriterImpl<ChunkRequest> {

    private volatile ChannelPromise endPromise;
    private volatile Channel channel;
    private volatile boolean http2;
    private volatile Http2ConnectionHandler h2Handler;
    private volatile int streamId;

    @Override
    public ChannelFuture writeAndFlush(ChunkRequest request,
                                       Channel channel,
                                       Context ctx,
                                       boolean uriEncodeEnabled,
                                       HttpVersion version,
                                       boolean http2) throws IOException {
        this.endPromise = channel.newPromise();
        this.channel = channel;

        return super.writeAndFlush(request, channel, ctx, uriEncodeEnabled, version, http2);
    }

    @Override
    ChannelFuture writeAndFlush2(ChunkRequest request,
                                 Channel channel,
                                 Context context,
                                 Http2ConnectionHandler handler,
                                 int streamId,
                                 boolean uriEncodeEnabled) {
        // Writes http2 headers
        ChannelFuture future = checkAndWriteH2Headers(channel,
                handler,
                toHttp2Headers(request, (Http1HeadersImpl) request.headers(), uriEncodeEnabled),
                streamId,
                false,
                channel.newPromise());
        if (future.isDone() && !future.isSuccess()) {
            return future;
        }

        h2Handler = handler;
        http2 = true;
        this.streamId = streamId;
        return endPromise;
    }

    @Override
    ChannelFuture writeAndFlush1(ChunkRequest request,
                                 Channel channel,
                                 Context context,
                                 HttpVersion version,
                                 boolean uriEncodeEnabled) {
        HttpRequest request0 = new DefaultHttpRequest(version,
                HttpMethod.valueOf(request.method().name()),
                request.uri().relative(uriEncodeEnabled),
                ((Http1HeadersImpl) request.headers()));

        if (StringUtils.isEmpty(request0.headers().get(HttpHeaderNames.CONTENT_LENGTH))
                && StringUtils.isEmpty(request0.headers().get(HttpHeaderNames.TRANSFER_ENCODING))) {
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger().debug("content-length and transfer-encoding are both absent, try to set" +
                        " default transfer-encoding: chunked, uri: {}", request.uri().toString());
            }
            HttpUtil.setTransferEncodingChunked(request0, true);
        }
        channel.write(request0);
        http2 = false;
        return endPromise;
    }

    <T> ChannelFuture write(T data, int offset, int length) {
        assert channel.eventLoop().inEventLoop();
        ByteBuf buf = null;
        try {
            if (data instanceof Buffer) {
                buf = ((Buffer) data).getByteBuf();
            } else if (data instanceof byte[]) {
                buf = channel.alloc().buffer(length);
                buf.writeBytes((byte[]) data, offset, length);
            } else {
                return channel.newFailedFuture(new IllegalArgumentException("Unexpected writable data format: "
                        + data.getClass() + ", expected(byte[], Buffer)"));
            }

            ChannelFuture future;
            if (http2) {
                future = h2Handler.writeData(streamId,
                        buf,
                        false,
                        channel.newPromise());
                channel.flush();
            } else {
                future = channel.writeAndFlush(new DefaultHttpContent(buf));
            }

            return future;
        } catch (Throwable ex) {
            Utils.tryRelease(buf);
            return channel.newFailedFuture(new IOException("Failed to write data to connection", ex));
        }
    }

    ChannelFuture end() {
        assert channel.eventLoop().inEventLoop();
        if (http2) {
            h2Handler.writeData(streamId, Unpooled.EMPTY_BUFFER, true, endPromise);
            channel.flush();
        } else {
            channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT, endPromise);
        }

        return endPromise;
    }

    ChannelFuture end(HttpHeaders trailers) {
        assert channel.eventLoop().inEventLoop();
        try {
            if (http2) {
                Http2Headers trailers0 = new DefaultHttp2Headers(HttpHeadersUtils.VALIDATE);
                HttpConversionUtil.toHttp2Headers(HttpHeadersUtils.toHttpHeaders(trailers), trailers0);
                h2Handler.writeHeaders(streamId,
                        trailers0,
                        true,
                        endPromise);
                channel.flush();
            } else {
                LastHttpContent content = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER,
                        HttpHeadersUtils.VALIDATE);
                content.trailingHeaders().add(HttpHeadersUtils.toHttpHeaders(trailers));
                channel.writeAndFlush(content, endPromise);
            }
        } catch (Throwable ex) {
            final Exception ex0;
            if (endPromise.isDone()) {
                ex0 = new IllegalStateException("Failed to end request, maybe has already ended");
            } else {
                ex0 = new IOException("Failed to end request, and the connection will be released automatically");
                try {
                    endPromise.setFailure(ex0);
                } catch (Throwable th) {
                    // Ignore
                }
            }
            return channel.newFailedFuture(ex0);
        }

        return endPromise;
    }

    void close(Throwable cause) {
        try {
            endPromise.setFailure(cause);
        } catch (Throwable ex) {
            // Ignore
        }
    }

    Channel channel() {
        if (channel != null) {
            return channel;
        }
        throw new IllegalStateException("Connection is null");
    }

}
