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

import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;

@FunctionalInterface
public interface RetryPredicate {

    /**
     * Whether to retry specified {@link HttpRequest} when {@code throwable} caught.
     *
     * @param request   request, must not be null
     * @param response  response, may be null
     * @param ctx       ctx, must be null
     * @param cause     cause, may be null
     * @return retry or not
     */
    boolean canRetry(HttpRequest request, HttpResponse response, Context ctx, Throwable cause);

}
