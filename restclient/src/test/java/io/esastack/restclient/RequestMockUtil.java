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

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.commons.net.netty.buffer.BufferImpl;
import io.esastack.commons.net.netty.http.CookieImpl;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.CompositeRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.MultipartBody;
import io.esastack.restclient.exec.RestRequestExecutor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestMockUtil {

    private RequestMockUtil() {

    }

    public static RestCompositeRequest mockRequest(
            RestClientOptions clientOptions,
            RestRequestExecutor requestExecutor,
            Object requestContent,
            String responseContent, String responseCookieName, String responseCookieValue) {
        CompositeRequest request = mock(CompositeRequest.class);
        when(request.headers()).thenReturn(new Http1HeadersImpl());
        RestCompositeRequest restRequest = new RestCompositeRequest(request, clientOptions, requestExecutor);
        if (requestContent instanceof byte[]) {
            restRequest.entity((byte[]) requestContent);
        } else if (requestContent instanceof File) {
            restRequest.entity((File) requestContent);
        } else if (requestContent instanceof MultipartBody) {
            restRequest.multipart();
        } else {
            restRequest.entity(requestContent);
        }
        when(request.execute())
                .thenReturn(CompletableFuture.completedFuture(
                        createResponse(responseContent, responseCookieName, responseCookieValue)));
        return restRequest;
    }

    private static HttpResponse createResponse(String content, String cookieName, String cookieValue) {
        return new HttpResponse() {
            @Override
            public Buffer body() {
                Buffer buffer = new BufferImpl();
                buffer.writeBytes(content.getBytes(StandardCharsets.UTF_8));
                return buffer;
            }

            @Override
            public boolean aggregated() {
                return true;
            }

            @Override
            public HttpHeaders trailers() {
                return null;
            }

            @Override
            public int status() {
                return 200;
            }

            @Override
            public HttpVersion version() {
                return HttpVersion.HTTP_1_1;
            }

            @Override
            public HttpHeaders headers() {
                HttpHeaders headers = new Http1HeadersImpl();
                headers.add(HttpHeaderNames.CONTENT_TYPE, MediaTypeUtil.TEXT_PLAIN.toString());
                Cookie cookie = new CookieImpl(cookieName, cookieValue);
                headers.add(HttpHeaderNames.SET_COOKIE, cookie.encode(true));
                return headers;
            }
        };
    }
}
