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

import esa.commons.http.HttpVersion;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.ContextNames;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.Listener;
import io.esastack.httpclient.core.ListenerProxy;
import io.esastack.httpclient.core.filter.ResponseFilter;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPool;

import java.util.concurrent.CompletableFuture;

abstract class TransceiverHandle {

    /**
     * Builds a {@link ListenerProxy} with supplied {@link Channel} and {@link Listener}.
     *
     * @param channel     channel
     * @param channelPool channel pool
     * @param delegate    listener
     * @param version     version
     * @return            proxied listener
     */
    abstract TimeoutHandle buildTimeoutHandle(Channel channel,
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
     * @param filters  filters
     * @param registry registry of handler adapter
     * @param response response
     * @return requestId
     */
    int addRspHandle(HttpRequest request,
                     Context ctx,
                     Channel channel,
                     Listener listener,
                     HandleImpl handle,
                     ResponseFilter[] filters,
                     HandleRegistry registry,
                     CompletableFuture<HttpResponse> response) {
        final NettyHandle nHandle = buildNettyHandle(request, ctx, channel, listener,
                handle, filters, response);
        return addRspHandle0(request, ctx, channel, nHandle, registry);
    }

    /**
     * Saves {@code handle} to {@link HandleRegistry} and returns the key of it.
     *
     * @param request  request
     * @param ctx      ctx
     * @param channel  channel
     * @param handle   handle
     * @param registry registry
     * @return id
     */
    abstract int addRspHandle0(HttpRequest request,
                               Context ctx,
                               Channel channel,
                               NettyHandle handle,
                               HandleRegistry registry);

    private NettyHandle buildNettyHandle(HttpRequest request,
                                         Context ctx,
                                         Channel channel,
                                         Listener listener,
                                         HandleImpl handle,
                                         ResponseFilter[] filters,
                                         CompletableFuture<HttpResponse> response) {
        if (handle == null) {
            handle = new DefaultHandle(channel.alloc());
        }

        if (filters == null || filters.length == 0) {
            return new NettyHandle(handle, request, ctx, listener, response);
        } else {
            return new FilteringHandle(handle, request, ctx, listener, response, filters,
                    ctx.removeAttr(ContextNames.FILTER_CONTEXT));
        }
    }
}
