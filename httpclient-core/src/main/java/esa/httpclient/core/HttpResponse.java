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
package esa.httpclient.core;

import esa.commons.http.HttpHeaders;
import esa.commons.netty.core.Buffer;

public interface HttpResponse extends HttpMessage {

    /**
     * Obtains body as {@link Buffer} format.
     *
     * @return body
     */
    Buffer body();

    /**
     * Obtains {@link HttpHeaders} of trailing.
     *
     * @return headers
     */
    HttpHeaders trailers();

    /**
     * The flag indicates current response should be automatically aggregated or not.
     * Be aware that, the result is {@code false} which means that you have specified a {@link Handle}
     * or {@link Handler} to handle the inbound message and so that the {@link #body()} may be
     * an empty buffer otherwise you have wrote some content while handling inbound message
     * by {@link Handle} or {@link Handler}. On the contrary, if the result is {@code true} which
     * means the {@link #body()} contains the fully response's content.
     *
     * @return  true or false
     */
    boolean aggregated();

}
