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
package esa.httpclient.core.exec;

import esa.httpclient.core.Context;
import esa.httpclient.core.Handle;
import esa.httpclient.core.Handler;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.netty.NettyHandle;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * The core transceiver which can transform and write the given {@code request} to network and
 * then aggregate the inbound messages to a {@code response} or handle those messages by customizing
 * {@link Handle} or {@link Handler}.
 */
public interface HttpTransceiver {

    /**
     * Send the given {@code request} and get the corresponding response.
     *
     * @param request     request
     * @param ctx         ctx
     * @param handle      handle
     * @param listener    listener
     * @param readTimeout readTimeout
     * @return response
     */
    CompletableFuture<HttpResponse> handle(HttpRequest request,
                                           Context ctx,
                                           BiFunction<Listener, CompletableFuture<HttpResponse>, NettyHandle> handle,
                                           Listener listener,
                                           int readTimeout);

}
