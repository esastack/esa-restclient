/*
 * Copyright 2021 OPPO ESA Stack Project
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
package esa.httpclient.core;

import java.util.concurrent.CompletableFuture;

/**
 * The {@link HttpRequest} which can be used to execute directly by {@link #execute()}.
 */
interface ExecutableRequest extends HttpRequestBase {

    /**
     * Sends current {@link HttpRequest} and obtains corresponding {@link HttpResponse}.
     *
     * Be aware that, if the {@link CompletableFuture} is returned, which means that we will release
     * the {@link HttpRequest#buffer()} automatically even if it's an exceptionally {@code future}.
     * Besides, you should manage the {@link HttpRequest#buffer()} by yourself. eg:
     * <pre>
     *     final Buffer buffer = xxx;
     *     try {
     *         client.execute();
     *     } catch (Throwable th) {
     *         buffer.getByteBuf().release();
     *     }
     * </pre>
     *
     * @return response
     */
    CompletableFuture<HttpResponse> execute();

}

