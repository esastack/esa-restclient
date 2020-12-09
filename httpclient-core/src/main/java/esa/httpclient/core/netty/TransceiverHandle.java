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
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.ListenerProxy;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPool;

import java.util.concurrent.CompletableFuture;

interface TransceiverHandle {

    /**
     * Builds a {@link ListenerProxy} with supplied {@link Channel} and {@link Listener}.
     *
     * @param channel     channel
     * @param channelPool channel pool
     * @param delegate    listener
     * @param version     version
     * @return            proxied listener
     */
    TimeoutHandle buildTimeoutHandle(Channel channel,
                                     ChannelPool channelPool,
                                     Listener delegate,
                                     HttpVersion version);

    /**
     * Builds a {@link NettyHandle} and adds it to {@link HandleRegistry}.
     *
     * @param request  request
     * @param ctx      ctx
     * @param channel  channel
     * @param listener listener
     * @param handle   handle
     * @param registry registry of handler adapter
     * @param response response
     * @return requestId
     */
    int addRspHandle(HttpRequest request,
                     Context ctx,
                     Channel channel,
                     Listener listener,
                     NettyHandle handle,
                     HandleRegistry registry,
                     CompletableFuture<HttpResponse> response);

}
