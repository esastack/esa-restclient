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

import esa.commons.netty.core.Buffer;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.exception.ClosedConnectionException;
import io.esastack.httpclient.core.exec.ExecContext;
import io.esastack.httpclient.core.util.HttpHeadersUtils;
import io.esastack.httpclient.core.util.LoggerUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

class PlainWriter extends RequestWriterImpl {

    private static final PlainWriter INSTANCE = new PlainWriter();

    private PlainWriter() {
    }

    @Override
    ChannelFuture writeAndFlush1(HttpRequest request,
                                 Channel channel,
                                 ExecContext execCtx,
                                 ChannelPromise headFuture,
                                 HttpVersion version,
                                 boolean uriEncodeEnabled) {
        addContentLengthIfAbsent(request, v -> request.buffer() == null ? 0L : request.buffer().readableBytes());

        if (request.buffer() == null || !request.buffer().isReadable()) {
            final DefaultFullHttpRequest req = new DefaultFullHttpRequest(version,
                    HttpMethod.valueOf(request.method().name()),
                    request.uri().relative(uriEncodeEnabled),
                    Unpooled.EMPTY_BUFFER,
                    (Http1HeadersImpl) request.headers(),
                    EmptyHttpHeaders.INSTANCE);

            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger().debug("Send Request:\n" + req);
            }
            return channel.writeAndFlush(req, headFuture);
        } else {
            final String uri = request.uri().relative(uriEncodeEnabled);
            channel.write(new DefaultHttpRequest(version,
                    HttpMethod.valueOf(request.method().name()),
                    uri,
                    (Http1HeadersImpl) request.headers()), headFuture);

            final ChannelPromise endPromise = channel.newPromise();
            if (writeContentNow(execCtx, request)) {
                Utils.runInChannel(channel, () -> doWriteContent1(channel, request.buffer(), endPromise));
            } else {
                channel.flush();
                execCtx.set100ContinueCallback(() ->
                        Utils.runInChannel(channel, () -> doWriteContent1(channel, request.buffer(), endPromise)));
            }

            return endPromise;
        }
    }

    private static void doWriteContent1(Channel channel,
                                        Buffer content,
                                        ChannelPromise endPromise) {
        // Note: retained the buffer so that channel can release it normally which has
        // no effect to us when we release the buffer after ending the request.
        //
        // slice the buffer so that we can reRead the original buffer for
        // retrying\redirecting and other purpose.

        // See https://github.com/esastack/esa-httpclient/issues/80
        if (!channel.isActive()) {
            endPromise.tryFailure(new ClosedConnectionException("Connection: " + channel + " inactive"));
            return;
        }

        final ByteBuf buf = content.getByteBuf().retainedSlice();
        channel.writeAndFlush(new DefaultLastHttpContent(buf), endPromise);
    }

    @Override
    ChannelFuture writeAndFlush2(HttpRequest request,
                                 Channel channel,
                                 ExecContext execCtx,
                                 ChannelPromise headFuture,
                                 Http2ConnectionHandler handler,
                                 int streamId,
                                 boolean uriEncodeEnabled) {
        final ChannelFuture future = checkAndWriteH2Headers(channel,
                handler,
                HttpHeadersUtils.toHttp2Headers(request, (Http1HeadersImpl) request.headers(), uriEncodeEnabled),
                streamId,
                false,
                headFuture);
        if ((future.isDone() && !future.isSuccess())) {
            return future;
        }

        // Note: retained the buffer so that channel can release it normally which has
        // no effect to us when we release the buffer after ending the request.
        //
        // slice the buffer so that we can reRead the original buffer for
        // retrying\redirecting and other purpose.
        final ByteBuf data = request.buffer() == null
                ? Unpooled.EMPTY_BUFFER : request.buffer().getByteBuf().retainedSlice();
        final ChannelPromise endPromise = channel.newPromise();
        if (writeContentNow(execCtx, request)) {
            doWriteContent2(channel,
                    data,
                    handler,
                    streamId,
                    endPromise);
        } else {
            channel.flush();
            execCtx.set100ContinueCallback(() ->
                    doWriteContent2(channel, data, handler, streamId, endPromise));
        }

        return endPromise;
    }

    private static void doWriteContent2(Channel channel,
                                        ByteBuf data,
                                        Http2ConnectionHandler handler,
                                        int streamId,
                                        ChannelPromise endPromise) {
        handler.writeData(
                streamId,
                data,
                true,
                endPromise);
        channel.flush();
    }

    static PlainWriter singleton() {
        return INSTANCE;
    }
}
