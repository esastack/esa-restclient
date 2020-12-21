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

import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.filter.DuplexFilter;
import esa.httpclient.core.filter.RequestFilter;
import esa.httpclient.core.filter.ResponseFilter;
import esa.httpclient.core.util.Ordered;

import java.util.concurrent.CompletableFuture;

/**
 * {@link Interceptor} is designed for handling retry, cache, redirect and so on.
 * Be different from {@link RequestFilter} and {@link ResponseFilter} is that you can replace {@link HttpRequest}
 * and {@link HttpResponse} when {@link #proceed(HttpRequest, ExecChain)}ing. The context among
 * {@link Interceptor} can be obtained by {@link ExecChain#ctx()}. More importantly, the most important difference
 * between {@link Interceptor} and {@link DuplexFilter} is that the former will only be executed once in every
 * {@link HttpClient#execute(HttpRequest)}, but the latter may be executed more than one times due to retrying,
 * redirecting and so on. And you can also think that the interceptors are strongly related to the original
 * invocation, such as {@link HttpClient#execute(HttpRequest)}, and the filters are strongly related to network
 * transmission. The order of multiple {@link Interceptor}s can be specified by {@link #getOrder()} and the
 * lowest value has the highest order.
 */
public interface Interceptor extends Ordered {

    /**
     * Proceed the {@link HttpRequest} and returns {@link HttpResponse}
     *
     * @param request request
     * @param next    chain
     * @return response
     */
    CompletableFuture<HttpResponse> proceed(HttpRequest request, ExecChain next);

}
