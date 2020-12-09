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

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.Context;
import esa.httpclient.core.Handle;
import esa.httpclient.core.Handler;
import esa.httpclient.core.HttpMessage;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class NettyHandle extends HandleImpl {

    private final AtomicBoolean ended = new AtomicBoolean();
    private final HttpRequest request;
    private final Context ctx;
    private final Listener listener;
    private final CompletableFuture<HttpResponse> response;

    long remaining = -1L;

    public NettyHandle(HttpRequest request,
                       Context ctx,
                       Listener listener,
                       CompletableFuture<HttpResponse> response) {
        super(new NettyResponse(true));
        Checks.checkNotNull(request, "HttpRequest must not be null");
        Checks.checkNotNull(ctx, "Context must not be null");
        Checks.checkNotNull(listener, "Listener must not be null");
        Checks.checkNotNull(response, "HttpResponse must not be null");
        this.request = request;
        this.ctx = ctx;
        this.listener = listener;
        this.response = response;
    }

    /**
     * Adapter for {@link Handler}.
     *
     * @param handler       handler
     * @param request       request
     * @param ctx           ctx
     * @param listener      listener
     * @param response      response
     */
    public NettyHandle(Handler handler,
                       HttpRequest request,
                       Context ctx,
                       Listener listener,
                       CompletableFuture<HttpResponse> response) {
        super(handler.response(), handler);
        Checks.checkNotNull(request, "HttpRequest must not be null");
        Checks.checkNotNull(ctx, "Context must not be null");
        Checks.checkNotNull(listener, "Listener must not be null");
        Checks.checkNotNull(response, "HttpResponse must not be null");
        this.request = request;
        this.ctx = ctx;
        this.listener = listener;
        this.response = response;
    }

    /**
     * Adapter for {@link Handle}.
     *
     * @param handle        handle
     * @param request       request
     * @param ctx           ctx
     * @param listener      listener
     * @param response      response
     */
    public NettyHandle(Consumer<Handle> handle,
                       HttpRequest request,
                       Context ctx,
                       Listener listener,
                       CompletableFuture<HttpResponse> response) {
        super(new NettyResponse(false), handle);
        Checks.checkNotNull(request, "HttpRequest must not be null");
        Checks.checkNotNull(ctx, "Context must not be null");
        Checks.checkNotNull(listener, "Listener must not be null");
        Checks.checkNotNull(response, "HttpResponse must not be null");
        this.request = request;
        this.ctx = ctx;
        this.listener = listener;
        this.response = response;
    }

    public void onMessage(HttpMessage message) {
        if (ended.get()) {
            return;
        }

        try {
            super.underlying.message(message);
            listener.onMessageReceived(request, ctx, message);
            if (super.start != null) {
                super.start.accept(null);
            }
        } catch (Throwable ex) {
            onError(ex);
        }
    }

    public void onData(Buffer content) {
        if (ended.get()) {
            return;
        }

        try {
            if (super.data != null) {
                super.data.accept(content);
            }
        } catch (Throwable ex) {
            onError(ex);
        }
    }

    public void onEnd() {
        if (ended.get()) {
            return;
        }

        try {
            if (ended.compareAndSet(false, true)) {
                if (super.end != null) {
                    super.end.accept(null);
                }
                response.complete(this);
                listener.onCompleted(request, ctx, this);
            }
        } catch (Throwable ex) {
            // Reset the ended flag so that onError can have chance to execute.
            onError0(ex);
        }
    }

    public void onError(Throwable cause) {
        if (ended.compareAndSet(false, true)) {
            onError0(cause);
        }
    }

    public void onTrailers(HttpHeaders trailers) {
        if (ended.get()) {
            return;
        }

        try {
            if (super.trailers != null) {
                super.trailers.accept(trailers);
            }
        } catch (Throwable ex) {
            onError(ex);
        }
    }

    public Context ctx() {
        return ctx;
    }

    private void onError0(Throwable cause) {
        response.completeExceptionally(cause);
        if (super.error != null) {
            super.error.accept(cause);
        }

        listener.onError(request, ctx, cause);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
