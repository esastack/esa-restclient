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
import io.esastack.httpclient.core.Handle;
import io.esastack.httpclient.core.Handler;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.Listener;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Internal
public interface RequestExecutor {

    /**
     * Sends the {@link HttpRequest} and obtains asynchronous {@link HttpResponse}.
     *
     * @param request       request
     * @param ctx           ctx
     * @param listener      listener
     * @param handle        handle
     * @param handler       handler
     * @return response
     */
    CompletableFuture<HttpResponse> execute(HttpRequest request,
                                            Context ctx,
                                            Listener listener,
                                            Consumer<Handle> handle,
                                            Handler handler);
}
