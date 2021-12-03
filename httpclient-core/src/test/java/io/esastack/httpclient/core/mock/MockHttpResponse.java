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
package io.esastack.httpclient.core.mock;

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.HttpResponse;

public class MockHttpResponse implements HttpResponse {

    private final int status;
    private final HttpHeaders headers = new Http1HeadersImpl();
    private final HttpHeaders trailers = new Http1HeadersImpl();

    public MockHttpResponse(int status) {
        this.status = status;
    }

    public MockHttpResponse() {
        this(200);
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public HttpVersion version() {
        return null;
    }

    @Override
    public Buffer body() {
        return Buffer.defaultAlloc().empty();
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public HttpHeaders trailers() {
        return trailers;
    }

    @Override
    public boolean aggregated() {
        return false;
    }
}
