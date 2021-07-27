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

import esa.httpclient.core.Handle;
import esa.httpclient.core.Handler;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;

import java.util.concurrent.CompletableFuture;

/**
 * The core transceiver which can transform and write the given {@link HttpRequest} to network and
 * then aggregate the inbound messages to the {@link HttpResponse} or handle those messages by custom
 * {@link Handle} or {@link Handler}.
 */
public interface HttpTransceiver {

    /**
     * Sends the given {@code request} and obtains the corresponding {@link HttpResponse}.
     *
     * @param request     request
     * @param execCtx     ctx
     * @return response
     */
    CompletableFuture<HttpResponse> handle(HttpRequest request, ExecContext execCtx);

}
