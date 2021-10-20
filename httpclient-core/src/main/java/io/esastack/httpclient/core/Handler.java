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

import esa.commons.http.HttpHeaders;
import esa.commons.netty.core.Buffer;
import io.esastack.httpclient.core.netty.NettyResponse;

/**
 * This is an another format of {@link Handle}, which can help user handle
 * inbound messages in a whole component. You can choose {@code this} or {@link Handle}
 * which one you prefer. The class is designed as thread-safe and all methods will execute
 * in a fixed IO-Thread.
 */
public abstract class Handler {

    private final NettyResponse underlying = new NettyResponse(false);

    /**
     * Be informed when receiving {@link HttpResponse#headers()}.
     */
    public void onStart() {

    }

    /**
     * Be informed while receiving response's content partly.
     *
     * @param content  content
     */
    public abstract void onData(Buffer content);

    /**
     * Be informed while ending the response.
     */
    public abstract void onEnd();

    /**
     * Be informed while any throwable caught.
     *
     * @param cause throwable
     */
    public abstract void onError(Throwable cause);

    /**
     * Be informed while receiving {@link HttpResponse#trailers()}.
     *
     * @param trailers      trailers
     */
    public void onTrailers(HttpHeaders trailers) {
        underlying.trailers().add(trailers);
    }

    public final NettyResponse response() {
        return underlying;
    }
}
