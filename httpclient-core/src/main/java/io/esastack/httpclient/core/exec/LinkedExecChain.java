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
package io.esastack.httpclient.core.exec;

import esa.commons.Checks;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.Listener;
import io.esastack.httpclient.core.netty.HandleImpl;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class LinkedExecChain implements ExecChain {

    private final Interceptor current;
    private final ExecChain next;
    private final Context ctx;

    private LinkedExecChain(Interceptor current,
                            ExecChain next,
                            Context ctx) {
        Checks.checkNotNull(ctx, "ctx");
        Checks.checkNotNull(next, "next");
        Checks.checkNotNull(current, "current");
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
                          HttpTransceiver transceiver,
                          BiFunction<Listener, CompletableFuture<HttpResponse>, HandleImpl> handle,
                          Context ctx,
                          Listener listener) {
        if (interceptors.length == 0) {
            return buildTransceiver(transceiver, handle, ctx, listener);
        }

        ExecChain chain = buildTransceiver(transceiver, handle, ctx, listener);
        for (int i = interceptors.length - 1; i >= 0; i--) {
            chain = new LinkedExecChain(interceptors[i], chain, ctx);
        }

        return chain;
    }

    private static ExecChain buildTransceiver(HttpTransceiver transceiver,
                                              BiFunction<Listener,
                                                      CompletableFuture<HttpResponse>, HandleImpl> handle,
                                              Context ctx,
                                              Listener listener) {
        return new ExecChain() {
            @Override
            public Context ctx() {
                return ctx;
            }

            @Override
            public CompletableFuture<HttpResponse> proceed(HttpRequest request) {
                return transceiver.handle(request, ctx, handle, listener);
            }
        };

    }
}
