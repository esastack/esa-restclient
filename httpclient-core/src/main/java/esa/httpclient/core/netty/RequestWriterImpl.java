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

import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpHeaderValues;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.httpclient.core.Context;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.Scheme;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

import static esa.httpclient.core.netty.Utils.CONNECT_INACTIVE;

abstract class RequestWriterImpl implements RequestWriter {

    private static final Predicate<HttpRequest> CONTENT_LENGTH_ABSENT = request -> {
        HttpHeaders headers0 = request.headers();
        return !headers0.contains(HttpHeaderNames.CONTENT_LENGTH) &&
                !headers0.contains(HttpHeaderNames.TRANSFER_ENCODING);
    };

    private static final Predicate<HttpRequest> CONTENT_TYPE_ABSENT = request ->
            !request.headers().contains(HttpHeaderNames.CONTENT_TYPE);

    private static final Predicate<HttpRequest> HOST_ABSENT = request
            -> !request.headers().contains(HttpHeaderNames.HOST);

    @Override
    public ChannelFuture writeAndFlush(HttpRequest request,
                                       Channel channel,
                                       Context ctx,
                                       ChannelPromise headFuture,
                                       boolean uriEncodeEnabled,
                                       io.netty.handler.codec.http.HttpVersion version,
                                       boolean http2) throws IOException {
        addHostIfAbsent(request, () -> computeHost(request.uri().netURI()));

        if (http2) {
            Http2ConnectionHandler handler = getH2Handler(channel);
            int streamId = request.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
            return writeAndFlush2(request,
                    channel,
                    ctx,
                    headFuture,
                    handler,
                    streamId,
                    uriEncodeEnabled);
        } else {
            return writeAndFlush1(request,
                    channel,
                    ctx,
                    headFuture,
                    version,
                    uriEncodeEnabled);
        }
    }

    /**
     * Decorating of {@link Http2ConnectionEncoder#writeHeaders(ChannelHandlerContext, int, Http2Headers, int,
     * boolean, ChannelPromise)} which can handle exhausted of streamId.
     *
     * @param channel   channel
     * @param streamId  streamId
     * @param handler   handler
     * @param headers   headers
     * @param endOfStream endOfStream or not
     * @param headFuture   headFuture
     * @return future of write
     */
    final ChannelFuture checkAndWriteH2Headers(Channel channel,
                                               Http2ConnectionHandler handler,
                                               Http2Headers headers,
                                               int streamId,
                                               boolean endOfStream,
                                               ChannelPromise headFuture) {
        if (streamId < 0) {
            headFuture.setFailure(new StreamIdExhaustedException("No more streams can be created on connection: "
                    + channel + "(local), and current connection will close gracefully"));

            // Simulate a GOAWAY being received due to stream exhaustion on this connection. We use the maximum
            // valid stream ID for the current peer.
            handler.writeGoAwayOnExhaustion(channel.newPromise());
            return headFuture;
        }

        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger().debug("Send Request:\n" +
                    headers);
        }
        return handler.writeHeaders(streamId, headers, endOfStream, headFuture);
    }

    /**
     * Do write and flush using {@link HttpVersion#HTTP_2}.
     *
     * @param request  request
     * @param channel  channel
     * @param context  context
     * @param headFuture headFuture
     * @param streamId channel
     * @param handler  handler
     * @param uriEncodeEnabled uriEncode or not
     *
     * @return future
     * @throws IOException ex
     */
    abstract ChannelFuture writeAndFlush2(HttpRequest request,
                                          Channel channel,
                                          Context context,
                                          ChannelPromise headFuture,
                                          Http2ConnectionHandler handler,
                                          int streamId,
                                          boolean uriEncodeEnabled) throws IOException;

    /**
     * Do write and flush using {@link HttpVersion#HTTP_1_1} or {@link HttpVersion#HTTP_1_0}.
     *
     * @param request request
     * @param channel channel
     * @param context context
     * @param headFuture headFuture
     * @param version version
     * @param uriEncodeEnabled enabled uriEncodeEnabled or not
     * @return future
     * @throws IOException ex
     */
    abstract ChannelFuture writeAndFlush1(HttpRequest request,
                                          Channel channel,
                                          Context context,
                                          ChannelPromise headFuture,
                                          io.netty.handler.codec.http.HttpVersion version,
                                          boolean uriEncodeEnabled) throws IOException;

    /**
     * Adds content-length to request's headers if absent
     *
     * @param request       request
     * @param contentLength content length
     */
    static void addContentLengthIfAbsent(HttpRequest request, ToLongFunction<HttpRequest> contentLength) {
        if (!CONTENT_LENGTH_ABSENT.test(request)) {
            return;
        }

        final long contentLengthVal = contentLength.applyAsLong(request);
        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger().debug("content-length is absent, try to set default value: {}, uri: {}",
                    contentLengthVal, request.uri().toString());
        }
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, contentLengthVal);
    }

    /**
     * Adds content-type to request's headers if absent
     *
     * @param request     request
     * @param contentType content type
     */
    static void addContentTypeIfAbsent(HttpRequest request, Supplier<CharSequence> contentType) {
        if (!CONTENT_TYPE_ABSENT.test(request)) {
            return;
        }

        final CharSequence contentTypeVal = contentType.get();
        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger().debug("content-type is absent, try to set default value: {}, uri: {}",
                    contentTypeVal, request.uri().toString());
        }
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, contentTypeVal);
    }

    static boolean writeContentNow(Context context, HttpRequest request) {
        return !request.headers().contains(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE, true);
    }

    static String computeHost(URI uri) {
        int port = uri.getPort();
        if (port <= 0) {
            return uri.getHost();
        }

        if (uri.getScheme().equalsIgnoreCase(Scheme.HTTP.name0())
                && uri.getPort() == Scheme.HTTP.port()) {
            return uri.getHost();
        }

        if (uri.getScheme().equalsIgnoreCase(Scheme.HTTPS.name0())
                && uri.getPort() == Scheme.HTTPS.port()) {
            return uri.getHost();
        }

        return uri.getHost() + ":" + uri.getPort();
    }

    private static void addHostIfAbsent(HttpRequest request, Supplier<String> host) {
        if (!HOST_ABSENT.test(request)) {
            return;
        }

        final String hostVal = host.get();
        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger().debug("host is absent, try to set default value: {}, uri: {}",
                    hostVal, request.uri().toString());
        }

        request.headers().set(HttpHeaderNames.HOST, host.get());
    }

    private static Http2ConnectionHandler getH2Handler(Channel channel) throws ConnectException {
        Http2ConnectionHandler handler;
        if ((handler = channel.pipeline().get(Http2ConnectionHandler.class)) != null) {
            return handler;
        }

        throw CONNECT_INACTIVE;
    }

    private static class StreamIdExhaustedException extends RuntimeException {

        private static final long serialVersionUID = 6638917105569802492L;

        private StreamIdExhaustedException(String msg) {
            super(msg);
        }

    }

}
