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

import esa.commons.http.HttpHeaderValues;
import esa.commons.http.HttpVersion;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.Listener;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPool;

import java.util.concurrent.atomic.AtomicBoolean;

class H1TransceiverHandle extends TransceiverHandle {

    @Override
    public TimeoutHandle buildTimeoutHandle(Channel channel,
                                            ChannelPool channelPool,
                                            Listener delegate,
                                            HttpVersion version) {
        return new H1Listener(channelPool, delegate, channel, version);
    }

    @Override
    int addRspHandle0(HttpRequest request,
                      Context ctx,
                      Channel channel,
                      NettyHandle handle,
                      HandleRegistry registry) {
        int requestId = registry.put(handle);
        ((Http1ChannelHandler) channel.pipeline().last()).updateRequestId(requestId);
        return requestId;
    }

    private static class H1Listener extends TimeoutHandle {
        private final AtomicBoolean released = new AtomicBoolean();
        private final ChannelPool channelPool;
        private final Channel channel;
        private final HttpVersion version;

        private H1Listener(ChannelPool channelPool,
                           Listener delegate,
                           Channel channel,
                           HttpVersion version) {
            super(delegate);
            this.channelPool = channelPool;
            this.channel = channel;
            this.version = version;
        }

        @Override
        public void onCompleted(HttpRequest request, Context ctx, HttpResponse response) {
            if (closeNow(response, version)) {
                channel.close();
            }
            if (released.compareAndSet(false, true)) {
                channelPool.release(channel);
            }

            super.onCompleted(request, ctx, response);
        }

        @Override
        public void onError(HttpRequest request, Context ctx, Throwable cause) {
            if (released.compareAndSet(false, true)) {
                channelPool.release(channel);
            }

            super.onError(request, ctx, cause);
        }

        private static boolean closeNow(HttpResponse response, HttpVersion version) {
            if (response.headers().contains(esa.commons.http.HttpHeaderNames.CONNECTION,
                    HttpHeaderValues.CLOSE, true)) {
                return true;
            }

            return !(HttpVersion.HTTP_1_1 == version ||
                    response.headers().contains(esa.commons.http.HttpHeaderNames.CONNECTION,
                            HttpHeaderValues.KEEP_ALIVE, true));
        }
    }

}
