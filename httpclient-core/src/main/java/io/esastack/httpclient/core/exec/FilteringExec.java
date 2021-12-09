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

import io.esastack.httpclient.core.ContextKeys;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.Listener;
import io.esastack.httpclient.core.filter.FilterContext;
import io.esastack.httpclient.core.filter.RequestFilter;
import io.esastack.httpclient.core.util.Futures;

import java.util.concurrent.CompletableFuture;

import static io.esastack.httpclient.core.exec.RequestExecutorImpl.LISTENER_KEY;

/**
 * This interceptor is designed to execute all {@link RequestFilter}s before
 * writing {@link HttpRequest} to channel.
 */
public class FilteringExec implements Interceptor {

    private final RequestFilter[] requestFilters;
    private final boolean requestFiltersAbsent;

    public FilteringExec(RequestFilter[] requestFilters) {
        this.requestFilters = requestFilters;
        this.requestFiltersAbsent = requestFilters == null || requestFilters.length == 0;
    }

    @Override
    public CompletableFuture<HttpResponse> proceed(HttpRequest request, ExecChain next) {
        final FilterContext ctx0 = new FilterContext(next.ctx());
        next.ctx().attrs().attr(ContextKeys.FILTER_CONTEXT_KEY).set(ctx0);
        Listener listener = next.ctx().attrs().attr(LISTENER_KEY).getAndRemove();
        if (listener != null) {
            listener.onInterceptorsEnd(request, next.ctx());
            listener.onFiltersStart(request, ctx0);
        }

        return applyRequestFilters(request, ctx0)
                .thenCompose(v -> next.proceed(request));
    }

    private CompletableFuture<Void> applyRequestFilters(HttpRequest request, FilterContext ctx) {
        if (requestFiltersAbsent) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = null;
        for (RequestFilter filter : this.requestFilters) {
            if (future == null) {
                future = filter.doFilter(request, ctx);
                continue;
            }

            future = future.thenCompose(value -> filter.doFilter(request, ctx));
        }

        return future == null ? Futures.completed() : future;
    }

}
