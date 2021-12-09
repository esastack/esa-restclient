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

import io.esastack.commons.net.http.HttpVersion;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.ContextKeys;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.Listener;
import io.esastack.httpclient.core.ListenerProxy;
import io.esastack.httpclient.core.exec.ExecContext;
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
     * @return proxied listener
     */
    abstract TimeoutHandle buildTimeoutHandle(Channel channel,
                                              ChannelPool channelPool,
                                              Listener delegate,
                                              HttpVersion version);

    /**
     * Builds a {@link ResponseHandle} and adds it to {@link HandleRegistry}.
     *
     * @param request  request
     * @param execCtx  ctx
     * @param channel  channel
     * @param filters  filters
     * @param registry registry of handler adapter
     * @param tHandle  timeout handle
     * @param response response
     * @return requestId
     */
    int addRspHandle(HttpRequest request,
                     ExecContext execCtx,
                     Channel channel,
                     ResponseFilter[] filters,
                     HandleRegistry registry,
                     TimeoutHandle tHandle,
                     CompletableFuture<HttpResponse> response) {
        final ResponseHandle nHandle = buildNettyHandle(request, tHandle, execCtx, channel,
                filters, response);
        return addRspHandle0(request, execCtx.ctx(), channel, nHandle, registry);
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
                               ResponseHandle handle,
                               HandleRegistry registry);

    private ResponseHandle buildNettyHandle(HttpRequest request,
                                            TimeoutHandle tHandle,
                                            ExecContext execCtx,
                                            Channel channel,
                                            ResponseFilter[] filters,
                                            CompletableFuture<HttpResponse> response) {
        HandleImpl handle = execCtx.handleImpl(request);
        if (handle == null) {
            handle = new DefaultHandle(channel.alloc());
        }

        if (filters == null || filters.length == 0) {
            return new ResponseHandle(handle, request, execCtx, tHandle, response);
        } else {

            return new FilteringHandle(handle, request, execCtx, tHandle, response, filters,
                    execCtx.ctx().attrs().attr(ContextKeys.FILTER_CONTEXT_KEY).getAndRemove());
        }
    }
}
