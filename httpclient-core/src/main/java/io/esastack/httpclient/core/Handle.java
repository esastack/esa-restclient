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
package io.esastack.httpclient.core;

import esa.commons.netty.core.Buffer;
import io.esastack.commons.net.http.HttpHeaders;

import java.util.function.Consumer;

/**
 * The class is designed to help user define custom handler to handle
 * the inbound message. The interface is thread-safe, because that all
 * the methods will be executed in a fixed thread.
 */
public interface Handle extends HttpResponse {

    /**
     * Sets the consumer to handle the start of response.
     *
     * @param h     handler
     * @return      handle
     */
    default Handle onStart(Consumer<Void> h) {
        return this;
    }

    /**
     * Sets the consumer to handle received {@link Buffer}.
     *
     * @param h     handler
     * @return      handle
     */
    Handle onData(Consumer<Buffer> h);

    /**
     * Sets the consumer to handle received {@link HttpHeaders}.
     *
     * @param h     handler
     * @return      handle
     */
    Handle onTrailer(Consumer<HttpHeaders> h);

    /**
     * Sets the consumer to handle the ending of current response.
     *
     * Be note that, only the one of follows will be invoked.
     * <ul>
     *     <li>{@link #onError(Consumer)}</li>
     *     <li>{@link #onEnd(Consumer)}</li>
     * </ul>
     *
     * @param h     handler
     * @return      handle
     */
    Handle onEnd(Consumer<Void> h);

    /**
     * Sets the consumer to handle throwable.
     *
     * Be note that, only the one of follows will be invoked.
     * <ul>
     *     <li>{@link #onError(Consumer)}</li>
     *     <li>{@link #onEnd(Consumer)}</li>
     * </ul>
     *
     * @param h     handler
     * @return      handle
     */
    Handle onError(Consumer<Throwable> h);

}
