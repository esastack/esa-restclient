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
package io.esastack.httpclient.core.filter;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.exec.Interceptor;
import io.esastack.httpclient.core.util.Ordered;

import java.util.concurrent.CompletableFuture;

/**
 * The {@link ResponseFilter} will be executed after every network transports. You can get more information
 * about the difference between filter and interceptor from {@link Interceptor}.
 */
public interface ResponseFilter extends Ordered {

    /**
     * Proceeds the {@link HttpResponse} with corresponding {@link FilterContext}.
     * The method will be executed after {@link HttpHeaders} received and
     * before response content arrived. You can end the returned {@link CompletableFuture}
     * exceptionally to terminate the request and subsequent response data will be discarded.
     *
     * @param request  request
     * @param response response
     * @param ctx      ctx
     * @return completable future
     */
    CompletableFuture<Void> doFilter(HttpRequest request,
                                     HttpResponse response,
                                     FilterContext ctx);

}
