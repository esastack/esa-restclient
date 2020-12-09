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

import esa.commons.Checks;
import esa.httpclient.core.Context;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.netty.NettyHandle;
import esa.httpclient.core.netty.NettyTransceiver;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * The default implementation of {@link LinkedExecChain}.
 */
public class LinkedExecChain implements ExecChain {

    private final Interceptor current;
    private final ExecChain next;
    private final Context ctx;

    private LinkedExecChain(Interceptor current,
                            ExecChain next,
                            Context ctx) {
        Checks.checkNotNull(ctx, "Context must not be null");
        Checks.checkNotNull(next, "ExecChain must not be null");
        Checks.checkNotNull(current, "Interceptor must not be null");
        this.ctx = ctx;
        this.current = current;
        this.next = next;
    }

    @Override
    public Context ctx() {
        return ctx;
    }

    @Override
    public CompletableFuture<HttpResponse> proceed(HttpRequest request) {
        return current.proceed(request, next);
    }

    static ExecChain from(Interceptor[] interceptors,
                          NettyTransceiver transceiver,
                          BiFunction<Listener, CompletableFuture<HttpResponse>, NettyHandle> handle,
                          Context ctx,
                          Listener listener,
                          int readTimeout) {
        if (interceptors.length == 0) {
            return transceiver(transceiver, handle, ctx, listener, readTimeout);
        }

        ExecChain chain = transceiver(transceiver, handle, ctx, listener, readTimeout);
        for (int i = interceptors.length - 1; i >= 0; i--) {
            chain = new LinkedExecChain(interceptors[i], chain, ctx);
        }

        return chain;
    }

    private static ExecChain transceiver(NettyTransceiver transceiver,
                                         BiFunction<Listener, CompletableFuture<HttpResponse>, NettyHandle> handle,
                                         Context ctx,
                                         Listener listener,
                                         int readTimeout) {
        return new ExecChain() {
            @Override
            public Context ctx() {
                return ctx;
            }

            @Override
            public CompletableFuture<HttpResponse> proceed(HttpRequest request) {
                return transceiver.handle(request, ctx, handle, listener, readTimeout);
            }
        };

    }
}
