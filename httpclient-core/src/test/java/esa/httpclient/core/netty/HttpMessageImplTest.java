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
package esa.httpclient.core.netty;

import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.httpclient.core.HttpMessage;
import esa.httpclient.core.util.HttpHeadersUtils;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Headers;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpMessageImplTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class,
                () -> new HttpMessageImpl(200, null, new Http1HeadersImpl()));

        assertThrows(NullPointerException.class,
                () -> new HttpMessageImpl(200, HttpVersion.HTTP_1_1, null));

        new HttpMessageImpl(200, HttpVersion.HTTP_1_1, new Http1HeadersImpl());
    }

    @Test
    void testGetter() {
        final int status = 200;
        final HttpVersion version = HttpVersion.HTTP_1_1;
        final HttpHeaders headers = new Http1HeadersImpl();

        final HttpMessage message = new HttpMessageImpl(status, version, headers);
        then(message.status()).isEqualTo(status);
        then(message.version()).isSameAs(version);
        then(message.headers()).isSameAs(headers);
    }

    @Test
    void testFrom1() {
        final io.netty.handler.codec.http.HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("a", "b");
        headers.add("c", "d");

        final HttpResponse response = new DefaultHttpResponse(io.netty.handler.codec.http.HttpVersion.HTTP_1_1,
                HttpResponseStatus.MULTI_STATUS, headers);
        HttpMessage message = HttpMessageImpl.from(response);
        then(message.version()).isSameAs(HttpVersion.HTTP_1_1);
        then(message.status()).isEqualTo(HttpResponseStatus.MULTI_STATUS.code());
        then(message.headers().size()).isEqualTo(3);
        then(message.headers().get("a")).isEqualTo("b");
        then(message.headers().get("c")).isEqualTo("d");
        then(message.headers().contains(HttpHeadersUtils.TTFB)).isTrue();
    }

    @Test
    void testFrom2() {
        final Http2Headers headers = new DefaultHttp2Headers();
        headers.status(HttpResponseStatus.OK.codeAsText());

        headers.add("a", "b");
        headers.add("x", "y");

        HttpMessage message = HttpMessageImpl.from(headers, 3);
        then(message.status()).isEqualTo(200);
        then(message.version()).isSameAs(HttpVersion.HTTP_2);
        then(message.headers().size()).isEqualTo(2);
        then(message.headers().get("a")).isEqualTo("b");
        then(message.headers().get("x")).isEqualTo("y");
    }

    @Test
    void testFrom2Error() {
        final Http2Headers headers = new DefaultHttp2Headers();
        assertThrows(RuntimeException.class, () -> HttpMessageImpl.from(headers, 3));
    }
}
