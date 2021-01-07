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
package esa.httpclient.core.filter;

import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.exec.Interceptor;
import esa.httpclient.core.util.Ordered;

import java.util.concurrent.CompletableFuture;

/**
 * The {@link RequestFilter} which will be executed before every network transports. You can get more information
 * about the difference between filter and interceptor from {@link Interceptor}.
 */
public interface RequestFilter extends Ordered {

    /**
     * Proceeds the {@link HttpRequest} with corresponding {@link FilterContext}.
     * This method will be executed before writing {@link HttpRequest}, and if
     * the returned {@link CompletableFuture} completed exceptionally and then
     * the current execution will be terminated.
     *
     * @param request request
     * @param ctx     ctx
     * @return future
     */
    CompletableFuture<Void> doFilter(HttpRequest request, FilterContext ctx);

}
