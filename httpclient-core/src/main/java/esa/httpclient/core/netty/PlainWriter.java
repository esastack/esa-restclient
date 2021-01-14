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

import esa.commons.netty.core.Buffer;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.httpclient.core.Context;
import esa.httpclient.core.PlainRequest;
import esa.httpclient.core.util.HttpHeadersUtils;
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

import java.io.IOException;

class PlainWriter extends RequestWriterImpl<PlainRequest> {

    private static final PlainWriter INSTANCE = new PlainWriter();

    private PlainWriter() {
    }

    @Override
    public ChannelFuture writeAndFlush(PlainRequest request,
                                       Channel channel,
                                       Context ctx,
                                       ChannelPromise headFuture,
                                       boolean uriEncodeEnabled,
                                       HttpVersion version,
                                       boolean http2) throws IOException {
        addContentLengthIfAbsent(request, v -> request.buffer() == null ? 0L : request.buffer().readableBytes());

        return super.writeAndFlush(request, channel, ctx, headFuture, uriEncodeEnabled, version, http2);
    }

    @Override
    ChannelFuture writeAndFlush1(PlainRequest request,
                                 Channel channel,
                                 Context context,
                                 ChannelPromise headFuture,
                                 HttpVersion version,
                                 boolean uriEncodeEnabled) {
        if (request.buffer() == null || !request.buffer().isReadable()) {
            return channel.writeAndFlush(new DefaultFullHttpRequest(version,
                    HttpMethod.valueOf(request.method().name()),
                    request.uri().relative(uriEncodeEnabled),
                    Unpooled.EMPTY_BUFFER,
                    (Http1HeadersImpl) request.headers(),
                    EmptyHttpHeaders.INSTANCE), headFuture);
        } else {
            final String uri = request.uri().relative(uriEncodeEnabled);
            channel.write(new DefaultHttpRequest(version,
                    HttpMethod.valueOf(request.method().name()),
                    uri,
                    (Http1HeadersImpl) request.headers()), headFuture);

            final ChannelPromise endPromise = channel.newPromise();
            if (writeContentNow(context)) {
                Utils.runInChannel(channel, () -> doWriteContent1(channel, request.buffer(), endPromise));
            } else {
                channel.flush();
                ((NettyContext) context).set100ContinueCallback(() ->
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

        final ByteBuf buf = content.getByteBuf().retainedSlice();
        channel.writeAndFlush(new DefaultLastHttpContent(buf), endPromise);
    }

    @Override
    ChannelFuture writeAndFlush2(PlainRequest request,
                                 Channel channel,
                                 Context context,
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
        if (writeContentNow(context)) {
            doWriteContent2(channel,
                    data,
                    handler,
                    streamId,
                    endPromise);
        } else {
            channel.flush();
            ((NettyContext) context).set100ContinueCallback(() ->
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
