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
package io.esastack.restclient;

import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.restclient.codec.impl.StringCodec;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestResponseBaseImplTest {

    @Test
    void testStatus() {
        RestRequestBase request = mock(RestRequestBase.class);
        HttpResponse response = mock(HttpResponse.class);
        RestClientOptions clientOptions = mock(RestClientOptions.class);
        RestResponse restResponse = new RestResponseBaseImpl(request, response, clientOptions);
        when(response.status()).thenReturn(200);
        then(restResponse.status()).isEqualTo(200);
        when(response.status()).thenReturn(300);
        then(restResponse.status()).isEqualTo(300);
        when(response.status()).thenReturn(100);
        then(restResponse.status()).isEqualTo(100);
        when(response.status()).thenReturn(-1);
        then(restResponse.status()).isEqualTo(-1);
        when(response.status()).thenReturn(0);
        then(restResponse.status()).isEqualTo(0);
    }

    @Test
    void testHeaders() {
        RestRequestBase request = mock(RestRequestBase.class);
        HttpResponse response = mock(HttpResponse.class);
        RestClientOptions clientOptions = mock(RestClientOptions.class);
        RestResponse restResponse = new RestResponseBaseImpl(request, response, clientOptions);
        HttpHeaders headers = new Http1HeadersImpl();
        headers.add("aaa", "aaa");
        headers.add("bbb", "bbb");
        when(response.headers()).thenReturn(headers);
        then(restResponse.headers().size()).isEqualTo(2);
        then(restResponse.headers().get("aaa")).isEqualTo("aaa");
        then(restResponse.headers().get("bbb")).isEqualTo("bbb");
        headers.add("aaa", "aaa1");
        then(restResponse.headers().getAll("aaa").size()).isEqualTo(2);
        then(restResponse.headers().getAll("aaa").get(1)).isEqualTo("aaa1");
        when(response.headers()).thenReturn(null);
        then(restResponse.headers()).isEqualTo(null);
    }

    @Test
    void testTrailers() {
        RestRequestBase request = mock(RestRequestBase.class);
        HttpResponse response = mock(HttpResponse.class);
        RestClientOptions clientOptions = mock(RestClientOptions.class);
        RestResponse restResponse = new RestResponseBaseImpl(request, response, clientOptions);
        HttpHeaders headers = new Http1HeadersImpl();
        headers.add("aaa", "aaa");
        headers.add("bbb", "bbb");
        when(response.trailers()).thenReturn(headers);
        then(restResponse.trailers().size()).isEqualTo(2);
        then(restResponse.trailers().get("aaa")).isEqualTo("aaa");
        then(restResponse.trailers().get("bbb")).isEqualTo("bbb");
        headers.add("aaa", "aaa1");
        then(restResponse.trailers().getAll("aaa").size()).isEqualTo(2);
        then(restResponse.trailers().getAll("aaa").get(1)).isEqualTo("aaa1");
        when(response.trailers()).thenReturn(null);
        then(restResponse.trailers()).isEqualTo(null);
    }

    @Test
    void testVersion() {
        RestRequestBase request = mock(RestRequestBase.class);
        HttpResponse response = mock(HttpResponse.class);
        RestClientOptions clientOptions = mock(RestClientOptions.class);
        RestResponse restResponse = new RestResponseBaseImpl(request, response, clientOptions);
        when(response.version()).thenReturn(HttpVersion.HTTP_1_0);
        then(restResponse.version()).isEqualTo(HttpVersion.HTTP_1_0);
        when(response.version()).thenReturn(HttpVersion.HTTP_1_1);
        then(restResponse.version()).isEqualTo(HttpVersion.HTTP_1_1);
        when(response.version()).thenReturn(null);
        then(restResponse.version()).isEqualTo(null);
    }

    @Test
    void testCookieOperate() {
        RestRequestBase request = mock(RestRequestBase.class);
        HttpResponse response = mock(HttpResponse.class);
        RestClientOptions clientOptions = mock(RestClientOptions.class);
        RestResponseBase restResponse = new RestResponseBaseImpl(request, response, clientOptions);
        HttpHeaders headers = new Http1HeadersImpl();

        when(response.headers()).thenReturn(headers);
        then(restResponse.cookiesMap().size()).isEqualTo(0);
        then(restResponse.headers().getAll(HttpHeaderNames.SET_COOKIE).size()).isEqualTo(0);

        headers.add(HttpHeaderNames.SET_COOKIE, "aaa=aaa1");
        headers.add(HttpHeaderNames.SET_COOKIE, "aaa=aaa2");
        headers.add(HttpHeaderNames.SET_COOKIE, "bbb=bbb1");
        headers.add(HttpHeaderNames.SET_COOKIE, "bbb=bbb2");
        headers.add(HttpHeaderNames.SET_COOKIE, "ccc=ccc1");
        headers.add(HttpHeaderNames.SET_COOKIE, "ccc=ccc2");

        //test get cookies
        then(restResponse.cookies("aaa").size()).isEqualTo(2);
        then(restResponse.cookies("aaa").get(0).value()).isEqualTo("aaa1");
        then(restResponse.cookies("aaa").get(1).value()).isEqualTo("aaa2");
        then(restResponse.cookies("bbb").size()).isEqualTo(2);
        then(restResponse.cookies("bbb").get(0).value()).isEqualTo("bbb1");
        then(restResponse.cookies("bbb").get(1).value()).isEqualTo("bbb2");
        then(restResponse.cookies("ccc").size()).isEqualTo(2);
        then(restResponse.cookies("ccc").get(0).value()).isEqualTo("ccc1");
        then(restResponse.cookies("ccc").get(1).value()).isEqualTo("ccc2");
        then(restResponse.headers().getAll(HttpHeaderNames.SET_COOKIE).size()).isEqualTo(6);
    }

    @Test
    void testBodyToEntity() throws Exception {
        RestRequestBase request = mock(RestRequestBase.class);
        HttpResponse response = mock(HttpResponse.class);
        RestClientOptions clientOptions = mock(RestClientOptions.class);
        RestResponseBase restResponse = new RestResponseBaseImpl(request, response, clientOptions);
        HttpHeaders headers = new Http1HeadersImpl();
        when(response.headers()).thenReturn(headers);
        when(response.body()).thenReturn(BufferUtil.buffer("Hello".getBytes(StandardCharsets.UTF_8)));
        when(request.decoder()).thenReturn(new StringCodec());

        //decodeAdvices is empty
        when(clientOptions.unmodifiableDecodeAdvices()).thenReturn(Collections.emptyList());
        then(restResponse.bodyToEntity(String.class)).isEqualTo("Hello");

        //decodeAdvices is not empty
        when(clientOptions.unmodifiableDecodeAdvices())
                .thenReturn(Arrays.asList(
                        context -> {
                            String result = (String) context.next();
                            return result + " Test1";
                        },
                        context -> {
                            String result = (String) context.next();
                            return result + " Test2";
                        }
                ));
        then(restResponse.bodyToEntity(String.class)).isEqualTo("Hello Test2 Test1");
    }
}
