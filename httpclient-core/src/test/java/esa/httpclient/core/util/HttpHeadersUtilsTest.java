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
package esa.httpclient.core.util;

import esa.commons.http.HttpHeaders;
import esa.commons.netty.http.EmptyHttpHeaders;
import esa.commons.netty.http.Http1HeadersAdaptor;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.commons.netty.http.Http2HeadersAdaptor;
import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpRequest;
import io.netty.handler.codec.http2.Http2Headers;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class HttpHeadersUtilsTest {

    @Test
    void testCopyFrom() {
        then(HttpHeadersUtils.copyFrom(null).isEmpty()).isTrue();
        then(HttpHeadersUtils.copyFrom(new Http1HeadersImpl()).isEmpty()).isTrue();

        final HttpHeaders origin0 = new Http1HeadersImpl();
        origin0.set("a", "b");
        origin0.set("x", "y");

        final HttpHeaders headers0 = HttpHeadersUtils.copyFrom(origin0);
        then(headers0.get("a")).isEqualTo("b");
        then(headers0.get("x")).isEqualTo("y");
        origin0.add("m", "n");
        then(headers0.get("m")).isNull();

        final HttpHeaders origin1 = new Http2HeadersAdaptor();
        origin1.set("a", "b");
        origin1.set("x", "y");

        final HttpHeaders headers1 = HttpHeadersUtils.copyFrom(origin1);
        then(headers1.get("a")).isEqualTo("b");
        then(headers1.get("x")).isEqualTo("y");
        origin1.add("m", "n");
        then(headers1.get("m")).isNull();
    }

    @Test
    void testToHttpHeaders() {
        then(HttpHeadersUtils.toHttpHeaders(null)).isSameAs(
                io.netty.handler.codec.http.EmptyHttpHeaders.INSTANCE);
        then(HttpHeadersUtils.toHttpHeaders(EmptyHttpHeaders.INSTANCE))
                .isSameAs(io.netty.handler.codec.http.EmptyHttpHeaders.INSTANCE);

        final HttpHeaders headers0 = new Http1HeadersImpl();
        then(HttpHeadersUtils.toHttpHeaders(headers0)).isSameAs(headers0);

        final HttpHeaders origin1 = new Http2HeadersAdaptor();
        origin1.set("a", "b");
        origin1.set("x", "y");

        final io.netty.handler.codec.http.HttpHeaders headers1 = HttpHeadersUtils.toHttpHeaders(origin1);
        then(headers1.get("a")).isEqualTo("b");
        then(headers1.get("x")).isEqualTo("y");
        origin1.add("m", "n");
        then(headers1.get("m")).isNull();

        final HttpHeaders origin2 = new Http1HeadersAdaptor();
        origin2.set("a", "b");
        origin2.set("x", "y");

        final io.netty.handler.codec.http.HttpHeaders headers2 = HttpHeadersUtils.toHttpHeaders(origin2);
        then(headers2.get("a")).isEqualTo("b");
        then(headers2.get("x")).isEqualTo("y");
        origin2.add("m", "n");
        then(headers2.get("m")).isNull();
    }

    @Test
    void testToHttp2Headers() {
        final Http1HeadersImpl headers = new Http1HeadersImpl();
        headers.add("A", "B");
        headers.add("X", "Y");

        headers.add("Cookie", "value1");
        headers.add("Cookie", "value2");

        final HttpRequest request = HttpClient.ofDefault()
                .post("https://127.0.0.1:8080/abc");
        request.headers().add(headers);

        final Http2Headers out = HttpHeadersUtils.toHttp2Headers(request, headers, false);
        then(out.path().toString()).isEqualTo("/abc");
        then(out.method().toString()).isEqualTo("POST");
        then(out.scheme().toString()).isEqualTo("https");
        then(out.authority().toString()).isEqualTo("127.0.0.1:8080");
        then(out.get("a").toString()).isEqualTo("B");
        then(out.get("x").toString()).isEqualTo("Y");

        then(out.getAll("cookie").size()).isEqualTo(2);
    }
}
