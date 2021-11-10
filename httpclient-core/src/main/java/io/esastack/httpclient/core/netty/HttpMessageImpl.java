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

import esa.commons.Checks;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.netty.http.Http1HeadersAdaptor;
import io.esastack.commons.net.netty.http.Http2HeadersAdaptor;
import io.esastack.httpclient.core.HttpMessage;
import io.esastack.httpclient.core.util.HttpHeadersUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;

import java.util.StringJoiner;

class HttpMessageImpl implements HttpMessage {

    private final int status;
    private final HttpVersion version;
    private final HttpHeaders headers;

    HttpMessageImpl(int status, HttpVersion version, HttpHeaders headers) {
        Checks.checkNotNull(version, "version");
        Checks.checkNotNull(headers, "headers");
        this.status = status;
        this.version = version;
        this.headers = headers;
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public HttpVersion version() {
        return version;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", HttpMessageImpl.class.getSimpleName() + "[", "]")
                .add("status=" + status)
                .add("version=" + version)
                .add("headers=" + headers)
                .toString();
    }

    static HttpMessage from(io.netty.handler.codec.http.HttpResponse message) {
        message.headers().add(HttpHeadersUtils.TTFB, System.currentTimeMillis());
        return new HttpMessageImpl(message.status().code(),
                io.netty.handler.codec.http.HttpVersion.HTTP_1_1 == message.protocolVersion()
                        ? HttpVersion.HTTP_1_1 : HttpVersion.HTTP_1_0,
                new Http1HeadersAdaptor(message.headers()));
    }

    static HttpMessage from(Http2Headers headers, int streamId) {
        try {
            HttpResponseStatus status = HttpConversionUtil.parseStatus(headers.status());
            Utils.standardHeaders(headers);
            return new HttpMessageImpl(status.code(),
                    HttpVersion.HTTP_2,
                    new Http2HeadersAdaptor(headers));
        } catch (Http2Exception ex) {
            throw new RuntimeException(String.format("Error whiling parsing HTTP2 headers, streamId: %d", streamId),
                    ex);
        }

    }
}
