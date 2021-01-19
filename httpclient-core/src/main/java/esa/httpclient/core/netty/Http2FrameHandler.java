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
import esa.commons.netty.core.BufferImpl;
import esa.commons.netty.http.Http2HeadersAdaptor;
import esa.httpclient.core.Context;
import esa.httpclient.core.exception.ClosedConnectionException;
import esa.httpclient.core.exception.ContentOverSizedException;
import esa.httpclient.core.util.HttpHeadersUtils;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpStatusClass;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2EventAdapter;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Stream;
import io.netty.handler.codec.http2.HttpConversionUtil;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http2.Http2Error.NO_ERROR;
import static io.netty.handler.codec.http2.Http2Error.PROTOCOL_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;
import static io.netty.handler.codec.http2.HttpConversionUtil.ExtensionHeaderNames.STREAM_ID;

class Http2FrameHandler extends Http2EventAdapter {

    private final HandleRegistry registry;
    private final Http2Connection connection;
    private final Http2Connection.PropertyKey messageKey;
    private final long maxContentLength;

    Http2FrameHandler(HandleRegistry registry,
                      Http2Connection connection,
                      long maxContentLength) {
        Checks.checkNotNull(registry, "HandleRegistry must not be null");
        Checks.checkNotNull(connection, "Http2Connection must not be null");
        this.connection = connection;
        this.messageKey = connection.newKey();
        this.registry = registry;
        this.connection.addListener(this);
        this.maxContentLength = maxContentLength;
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx,
                              int streamId,
                              Http2Headers headers,
                              int padding,
                              boolean endOfStream) throws Http2Exception {
        final Http2Stream stream = connection.stream(streamId);

        headers.setInt(STREAM_ID.text(), streamId);
        headers.addLong(HttpHeadersUtils.TTFB, System.currentTimeMillis());
        onHeaders(streamId, stream, headers, stream.getProperty(messageKey) != null, endOfStream);
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx,
                              int streamId,
                              Http2Headers headers,
                              int streamDependency,
                              short weight,
                              boolean exclusive,
                              int padding,
                              boolean endOfStream) throws Http2Exception {
        // Add headers for dependency and weight.
        // See https://github.com/netty/netty/issues/5866
        if (streamDependency != Http2CodecUtil.CONNECTION_STREAM_ID) {
            headers.setInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(),
                    streamDependency);
        }
        headers.setShort(HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(), weight);

        onHeadersRead(ctx, streamId, headers, padding, endOfStream);
    }

    @Override
    public int onDataRead(ChannelHandlerContext ctx,
                          int streamId,
                          ByteBuf data,
                          int padding,
                          boolean endOfStream) {
        final NettyHandle handle = registry.get(streamId);
        if (handle == null) {
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger().debug(String.format("Data Frame received for unknown stream id %d", streamId));
            }
            return data.readableBytes() + padding;
        }

        final int readableBytes = data.readableBytes();
        if (readableBytes > 0) {
            ByteBuf handledData;
            boolean exceeded = false;
            if (handle.remaining == -1L) {
                handledData = data;
            } else {
                handle.remaining -= readableBytes;
                if (handle.remaining >= 0L) {
                    handledData = data;
                } else {
                    handle.remaining += readableBytes;
                    handledData = data.slice(0, (int) handle.remaining);
                    exceeded = true;
                    handle.remaining = 0L;
                }
            }
            handle.onData(new BufferImpl(handledData.duplicate()));
            if (exceeded) {
                String errMsg = String.format("Content length exceeded %d bytes", maxContentLength);
                onError(new ContentOverSizedException(errMsg), null, streamId, true);
                return readableBytes + padding;
            }
        }

        if (endOfStream) {
            handle.onEnd();
            final Http2Stream stream = connection.stream(streamId);
            if (stream != null) {
                stream.removeProperty(messageKey);
            }
            registry.remove(streamId);
        }

        // All bytes have been processed.
        return readableBytes + padding;
    }

    @Override
    public void onRstStreamRead(ChannelHandlerContext ctx,
                                int streamId,
                                long errorCode) {
        final Http2Stream stream = connection.stream(streamId);
        if (stream != null) {
            onError(Http2Exception.streamError(streamId, Http2Error.valueOf(errorCode),
                    "Received reset stream"), stream, streamId, true);
        }
    }

    @Override
    public void onGoAwayRead(ChannelHandlerContext ctx, int lastStreamId,
                             long errorCode, ByteBuf debugData) throws Http2Exception {
        final String errMsg = debugData.toString(StandardCharsets.UTF_8);

        final ClosedConnectionException ex;
        if (NO_ERROR.code() == errorCode) {
            ex = new ClosedConnectionException("Received goAway stream in connection: " + ctx.channel()
                    + ", maybe server has closed the connection");
        } else {
            ex = new ClosedConnectionException("Received goAway stream in connection: " +
                    ctx.channel() + ", msg: " + errMsg);
        }

        // Ends the stream which id > lastStreamId with ConnectionException.
        connection.forEachActiveStream(stream -> {
            if (stream.id() > lastStreamId) {
                try {
                    onError(ex, stream, stream.id(), false);
                } catch (Throwable ignore) {

                }
            }

            return true;
        });

        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger().debug(ex.getMessage());
        }
    }

    @Override
    public void onPushPromiseRead(ChannelHandlerContext ctx,
                                  int streamId,
                                  int promisedStreamId,
                                  Http2Headers headers,
                                  int padding) throws Http2Exception {
        // A push promise should not be allowed to add headers to an existing stream
        Http2Stream promisedStream = connection.stream(promisedStreamId);
        if (promisedStream.getProperty(messageKey) != null) {
            throw connectionError(PROTOCOL_ERROR, "Push Promise Frame received for pre-existing stream id %d",
                    promisedStreamId);
        }

        if (headers.status() == null) {
            headers.status(OK.codeAsText());
        }

        headers.setShort(HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(),
                Http2CodecUtil.DEFAULT_PRIORITY_WEIGHT);
        headers.setInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_PROMISE_ID.text(), streamId);

        onHeaders(promisedStreamId, promisedStream, headers, false, false);
    }

    @Override
    public void onStreamRemoved(Http2Stream stream) {
        try {
            super.onStreamRemoved(stream);
        } finally {
            onError(Http2Exception.streamError(stream.id(), Http2Error.STREAM_CLOSED, "Stream removed"),
                    stream, -1, false);
        }
    }

    private void onHeaders(int streamId,
                           Http2Stream stream,
                           Http2Headers headers,
                           boolean trailer,
                           boolean endOfStream) throws Http2Exception {
        final NettyHandle handle = registry.get(streamId);

        if (handle == null) {
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger().debug(String.format("Data Frame received for unknown stream id %d", streamId));
            }
            return;
        }

        // Handle informational status
        final HttpResponseStatus status = HttpConversionUtil.parseStatus(headers.status());
        if (HttpStatusClass.INFORMATIONAL == status.codeClass()) {
            stream.removeProperty(messageKey);
            if (HttpResponseStatus.CONTINUE.code() == status.code()) {
                handle100Continue(handle.ctx());
            }
            return;
        }

        if (!headers.isEmpty()) {
            if (trailer) {
                handle.onTrailers(new Http2HeadersAdaptor(Utils.standardHeaders(headers)));
            } else {
                handle.onMessage(HttpMessageImpl.from(headers, streamId));
                if (isContentLengthInvalid(headers, handle)) {
                    String errMsg = String.format("Content length exceeded %d bytes", maxContentLength);
                    onError(new ContentOverSizedException(errMsg), stream, streamId, true);
                    return;
                }
                stream.setProperty(messageKey, true);
            }
        }

        if (endOfStream) {
            stream.removeProperty(messageKey);
            handle.onEnd();
            registry.remove(streamId);
        }
    }

    private void onError(Throwable cause, Http2Stream stream, int streamId, boolean enableLog) {
        if (stream != null) {
            stream.removeProperty(messageKey);
            streamId = stream.id();
        }

        // May be the handle has ended before, such as timeout, ended normally.
        NettyHandle handle = registry.remove(streamId);
        Utils.handleException(handle, cause, enableLog);
    }

    private void handle100Continue(Context ctx) {
        final Runnable runnable = ((NettyContext) ctx).remove100ContinueCallback();
        if (runnable != null) {
            runnable.run();
        }
    }

    private boolean isContentLengthInvalid(Http2Headers headers, NettyHandle handle) {
        if (maxContentLength > 0L) {
            long contentLength;
            try {
                contentLength = headers.getLong(HttpHeaderNames.CONTENT_LENGTH, -1L);
            } catch (NumberFormatException ex) {
                contentLength = -1L;
            }

            if (contentLength > maxContentLength) {
                return true;
            }

            handle.remaining = contentLength > 0L ? contentLength : maxContentLength;
            return false;
        }

        return false;
    }

}
