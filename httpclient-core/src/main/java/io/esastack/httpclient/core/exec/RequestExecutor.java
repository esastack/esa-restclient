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
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;

import java.util.concurrent.CompletableFuture;

/**
 * A {@link RequestExecutor} is used to send a {@link HttpRequest} and get the corresponding {@link HttpResponse}
 * asynchronously.
 */
@Internal
public interface RequestExecutor {

    /**
     * Sends the {@link HttpRequest} and obtains asynchronous {@link HttpResponse}.
     *
     * @param request       request
     * @param ctx           ctx
     * @return response
     */
    CompletableFuture<HttpResponse> execute(HttpRequest request, ExecContext ctx);
}
