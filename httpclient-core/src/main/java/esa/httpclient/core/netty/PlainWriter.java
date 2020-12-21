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

import static esa.httpclient.core.ContextNames.EXPECT_CONTINUE_CALLBACK;

class PlainWriter extends RequestWriterImpl<PlainRequest> {

    private static final byte[] EMPTY_DATA = new byte[0];
    private static final PlainWriter INSTANCE = new PlainWriter();

    private PlainWriter() {
    }

    @Override
    public ChannelFuture writeAndFlush(PlainRequest request,
                                       Channel channel,
                                       Context ctx,
                                       boolean uriEncodeEnabled,
                                       HttpVersion version,
                                       boolean http2) throws IOException {
        addContentLengthIfAbsent(request, v -> request.body() == null ? 0L : request.body().length);

        return super.writeAndFlush(request, channel, ctx, uriEncodeEnabled, version, http2);
    }

    @Override
    ChannelFuture writeAndFlush1(PlainRequest request,
                                 Channel channel,
                                 Context context,
                                 HttpVersion version,
                                 boolean uriEncodeEnabled) {
        if (request.body() == null || request.body().length == 0) {
            return channel.writeAndFlush(new DefaultFullHttpRequest(version,
                    HttpMethod.valueOf(request.method().name()),
                    request.uri().relative(uriEncodeEnabled),
                    Unpooled.EMPTY_BUFFER,
                    (Http1HeadersImpl) request.headers(),
                    EmptyHttpHeaders.INSTANCE));
        } else {
            final String uri = request.uri().relative(uriEncodeEnabled);
            channel.write(new DefaultHttpRequest(version,
                    HttpMethod.valueOf(request.method().name()),
                    uri,
                    (Http1HeadersImpl) request.headers()));

            final ChannelPromise endPromise = channel.newPromise();
            if (writeContentNow(context)) {
                Utils.runInChannel(channel, () -> doWriteContent1(channel, request.body(), endPromise));
            } else {
                channel.flush();
                context.setAttr(EXPECT_CONTINUE_CALLBACK, (Runnable) () ->
                        Utils.runInChannel(channel, () -> doWriteContent1(channel, request.body(), endPromise)));
            }

            return endPromise;
        }
    }

    private static void doWriteContent1(Channel channel,
                                        byte[] content,
                                        ChannelPromise endPromise) {
        ByteBuf buf = channel.alloc().buffer(content.length).writeBytes(content);
        try {
            channel.writeAndFlush(new DefaultLastHttpContent(buf), endPromise);
        } catch (Throwable ex) {
            endPromise.setFailure(ex);
            Utils.tryRelease(buf);
        }
    }

    @Override
    ChannelFuture writeAndFlush2(PlainRequest request,
                                 Channel channel,
                                 Context context,
                                 Http2ConnectionHandler handler,
                                 int streamId,
                                 boolean uriEncodeEnabled) {
        final ChannelFuture future = checkAndWriteH2Headers(channel,
                handler,
                HttpHeadersUtils.toHttp2Headers(request, (Http1HeadersImpl) request.headers(), uriEncodeEnabled),
                streamId,
                false,
                channel.newPromise());
        if ((future.isDone() && !future.isSuccess())) {
            return future;
        }

        final byte[] data = request.body() == null ? EMPTY_DATA : request.body();
        final ChannelPromise endPromise = channel.newPromise();
        if (writeContentNow(context)) {
            doWriteContent2(channel,
                    data,
                    handler,
                    streamId,
                    endPromise);
        } else {
            channel.flush();
            context.setAttr(EXPECT_CONTINUE_CALLBACK, (Runnable) () ->
                    doWriteContent2(channel, data, handler, streamId, endPromise));
        }

        return endPromise;
    }

    private static void doWriteContent2(Channel channel,
                                        byte[] data,
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
