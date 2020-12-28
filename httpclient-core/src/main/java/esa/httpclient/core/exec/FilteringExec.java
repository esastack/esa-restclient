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

import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.filter.FilterContextImpl;
import esa.httpclient.core.filter.RequestFilter;
import esa.httpclient.core.filter.ResponseFilter;
import esa.httpclient.core.util.Futures;

import java.util.concurrent.CompletableFuture;

import static esa.httpclient.core.exec.RequestExecutorImpl.LISTENER_KEY;

/**
 * This interceptor is designed to execute {@link RequestFilter} and {@link ResponseFilter} around
 * every network transports.
 */
public class FilteringExec implements Interceptor {

    private final RequestFilter[] requestFilters;
    private final boolean requestFiltersAbsent;

    private final ResponseFilter[] responseFilters;
    private final boolean responseFiltersAbsent;

    public FilteringExec(RequestFilter[] requestFilters, ResponseFilter[] responseFilters) {
        this.requestFilters = requestFilters;
        this.requestFiltersAbsent = requestFilters == null || requestFilters.length == 0;
        this.responseFilters = responseFilters;
        this.responseFiltersAbsent = responseFilters == null || responseFilters.length == 0;
    }

    @Override
    public CompletableFuture<HttpResponse> proceed(HttpRequest request, ExecChain next) {
        FilterContextImpl ctx0 = new FilterContextImpl(next.ctx());

        Listener listener = next.ctx().removeUncheckedAttr(LISTENER_KEY);
        if (listener != null) {
            listener.onInterceptorsEnd(request, next.ctx());
            listener.onFiltersStart(request, ctx0);
        }

        return applyRequestFilters(request, ctx0)
                .thenCompose(v -> next.proceed(request))
                .thenCompose(rsp -> applyResponseFilters(rsp, ctx0))
                .whenComplete((rsp, th) -> ctx0.clear());
    }

    private CompletableFuture<Void> applyRequestFilters(HttpRequest request, FilterContextImpl ctx) {
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

    private CompletableFuture<HttpResponse> applyResponseFilters(HttpResponse response,
                                                                 FilterContextImpl ctx) {
        if (responseFiltersAbsent) {
            return Futures.completed(response);
        }

        CompletableFuture<Void> future = null;

        // Executes response interceptors in IO threads directly.
        for (ResponseFilter filter : this.responseFilters) {
            if (future == null) {
                future = filter.doFilter(response, ctx);
            } else {
                future = future.thenCompose(v -> filter.doFilter(response, ctx));
            }
        }

        return future == null ? Futures.completed(response) :
                future.thenCompose(v -> Futures.completed(response));
    }

}
