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
import esa.httpclient.core.filter.RequestFilter;
import esa.httpclient.core.filter.ResponseFilter;
import esa.httpclient.core.util.Ordered;

import java.util.concurrent.CompletableFuture;

/**
 * {@link Interceptor} is designed for handling retry, cache, redirect and so on.
 * Be different from {@link RequestFilter} and {@link ResponseFilter} is that you can replace {@link HttpRequest}
 * and {@link HttpResponse} when {@link #proceed(HttpRequest, ExecChain)}ing. The context among
 * {@link Interceptor} can be obtained by {@link ExecChain#ctx()}. The order of multiple {@link Interceptor}s
 * can be specified by {@link #getOrder()} and the lowest value has the highest order.
 */
public interface Interceptor extends Ordered {

    /**
     * Proceeds the {@link HttpRequest} and obtains {@link HttpResponse}
     *
     * @param request request
     * @param next    chain
     * @return response
     */
    CompletableFuture<HttpResponse> proceed(HttpRequest request, ExecChain next);

}
