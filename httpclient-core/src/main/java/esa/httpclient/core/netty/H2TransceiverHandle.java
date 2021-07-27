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

import esa.commons.http.HttpVersion;
import esa.httpclient.core.Context;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.Listener;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPool;
import io.netty.handler.codec.http2.HttpConversionUtil;

import java.util.concurrent.atomic.AtomicBoolean;

class H2TransceiverHandle extends TransceiverHandle {

    @Override
    public TimeoutHandle buildTimeoutHandle(Channel channel,
                                            ChannelPool channelPool,
                                            Listener delegate,
                                            HttpVersion version) {
        return new H2Listener(delegate, channelPool, channel);
    }

    @Override
    int addRspHandle0(HttpRequest request,
                      Context ctx,
                      Channel channel,
                      ResponseHandle handle,
                      HandleRegistry registry) {
        int requestId = registry.put(handle);
        request.headers().set(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(),
                String.valueOf(requestId));
        return requestId;
    }

    private static class H2Listener extends TimeoutHandle {

        private final AtomicBoolean released = new AtomicBoolean();
        private final ChannelPool channelPool;
        private final Channel channel;

        private H2Listener(Listener delegate,
                           ChannelPool channelPool,
                           Channel channel) {
            super(delegate);
            this.channelPool = channelPool;
            this.channel = channel;
        }

        @Override
        public void onWriteDone(HttpRequest request, Context ctx) {
            if (released.compareAndSet(false, true)) {
                channelPool.release(channel);
            }

            super.onWriteDone(request, ctx);
        }

        @Override
        public void onError(HttpRequest request, Context ctx, Throwable cause) {
            if (released.compareAndSet(false, true)) {
                channelPool.release(channel);
            }

            super.onError(request, ctx, cause);
        }
    }
}
