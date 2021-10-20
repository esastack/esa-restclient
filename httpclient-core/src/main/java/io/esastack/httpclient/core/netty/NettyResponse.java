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
package io.esastack.httpclient.core.netty;

import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.commons.netty.core.Buffer;
import esa.commons.netty.core.Buffers;
import esa.commons.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.HttpMessage;
import io.esastack.httpclient.core.HttpResponse;

public class NettyResponse implements HttpResponse {

    private final HttpHeaders trailers = new Http1HeadersImpl();
    private final boolean aggregated;

    private volatile HttpMessage message;
    private volatile Buffer body = Buffers.buffer(0);

    public NettyResponse(boolean aggregated) {
        this.aggregated = aggregated;
    }

    @Override
    public HttpVersion version() {
        return message.version();
    }

    @Override
    public int status() {
        return message.status();
    }

    @Override
    public Buffer body() {
        return body == null ? Buffers.EMPTY_BUFFER : body;
    }

    @Override
    public HttpHeaders headers() {
        return message.headers();
    }

    @Override
    public HttpHeaders trailers() {
        return trailers;
    }

    @Override
    public boolean aggregated() {
        return aggregated;
    }

    void message(HttpMessage message) {
        this.message = message;
    }

    void body(Buffer body) {
        this.body = body;
    }
}
