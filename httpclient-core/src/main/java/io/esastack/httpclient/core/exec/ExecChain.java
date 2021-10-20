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

import esa.commons.annotation.Internal;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;

import java.util.concurrent.CompletableFuture;

/**
 * This chain is designed for {@link Interceptor}s to modify or replace {@link HttpRequest}
 * and {@link HttpResponse} around {@link #proceed(HttpRequest)}ing. User can do retry, redirect,
 * cache and so on by this.
 */
@Internal
public interface ExecChain {

    /**
     * Obtains the {@link Context} associated with current chain.
     *
     * @return ctx
     */
    Context ctx();

    /**
     * Proceeds {@link HttpRequest} and obtains asynchronous result.
     *
     * @param request request
     * @return response
     */
    CompletableFuture<HttpResponse> proceed(HttpRequest request);

}
