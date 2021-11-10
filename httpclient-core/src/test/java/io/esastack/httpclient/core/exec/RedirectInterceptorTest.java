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
package io.esastack.httpclient.core.exec;

import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaderValues;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.SegmentRequest;
import io.esastack.httpclient.core.exception.RedirectException;
import io.esastack.httpclient.core.mock.MockContext;
import io.esastack.httpclient.core.mock.MockHttpResponse;
import io.esastack.httpclient.core.util.Futures;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static io.esastack.httpclient.core.exec.RedirectInterceptor.HAS_REDIRECTED_COUNT;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedirectInterceptorTest {

    private static final String DO_REDIRECT = "$doRedirect";

    private final HttpClient client = HttpClient.ofDefault();

    @Test
    void testProceed() {
        final ExecChain next = mock(ExecChain.class);
        final MockContext ctx = new MockContext();
        when(next.ctx()).thenReturn(ctx);

        // segment request is not allowed to redirect
        final SegmentRequest request0 = client.post("http://127.0.0.1:8080/abc").segment();
        final HttpResponse response = new MockHttpResponse();
        response.headers().add(HttpHeaderNames.LOCATION, "127.0.0.1:9999");
        when(next.proceed(request0)).thenReturn(Futures.completed(response));

        final RedirectInterceptor interceptor = new AuxiliaryRedirectInterceptor();
        final CompletableFuture<HttpResponse> response00 = interceptor.proceed(request0, next);
        then((Integer) ctx.getAttr(DO_REDIRECT)).isNull();
        then(response00.isDone()).isTrue();
        then(response00.getNow(null)).isSameAs(response);
        ctx.clear();

        // redirect is not allowed as default
        final HttpRequest request1 = client.get("http://127.0.0.1:8080/abc");
        when(next.proceed(request1)).thenReturn(Futures.completed(response));
        final CompletableFuture<HttpResponse> response11 = interceptor.proceed(request1, next);
        then((Integer) ctx.getAttr(DO_REDIRECT)).isNull();
        then(response11.isDone()).isTrue();
        then(response11.getNow(null)).isSameAs(response);
        ctx.clear();

        // redirect is configured as false
        final CompletableFuture<HttpResponse> response22 = interceptor.proceed(request1, next);
        then((Integer) ctx.getAttr(DO_REDIRECT)).isNull();
        then(response22.isDone()).isTrue();
        then(response22.getNow(null)).isSameAs(response);
        ctx.clear();

        // redirect is configured as true
        ctx.maxRedirects(1);
        final CompletableFuture<HttpResponse> response33 = interceptor.proceed(request1, next);
        then((Boolean) ctx.getAttr(DO_REDIRECT)).isEqualTo(true);
        then(response33.isDone()).isTrue();
        then(response33.getNow(null)).isSameAs(AuxiliaryRedirectInterceptor.RESPONSE);
        ctx.clear();
    }

    @Test
    void testDoRedirect() {
        final RedirectInterceptor interceptor = new AuxiliaryRedirectInterceptor1();

        // Case 1: when complete exceptionally
        final ExecChain next = mock(ExecChain.class);
        final MockContext ctx = new MockContext();
        ctx.maxRedirects(10);
        when(next.ctx()).thenReturn(ctx);

        final HttpRequest request0 = client.get("http://127.0.0.1:8080/abc");
        when(next.proceed(request0)).thenReturn(Futures.completed(new IOException()));
        final CompletableFuture<HttpResponse> response00 = interceptor.proceed(request0, next);
        then(response00.isDone()).isTrue();
        then(response00.isCompletedExceptionally()).isTrue();
        ctx.clear();

        // Case 2: when complete normally
        ctx.maxRedirects(10);
        final HttpResponse response2 = new MockHttpResponse();
        when(next.proceed(request0)).thenReturn(Futures.completed(response2));
        final CompletableFuture<HttpResponse> response22 = interceptor.proceed(request0, next);
        then(response22.isDone()).isTrue();
        then(response22.isCompletedExceptionally()).isFalse();
        then(response22.getNow(null)).isSameAs(response2);
        ctx.clear();

        // Case 3: when complete with location, status is 200
        ctx.maxRedirects(10);
        final HttpResponse response3 = new MockHttpResponse();
        response3.headers().add(HttpHeaderNames.LOCATION, "127.0.0.1:9999");
        when(next.proceed(request0)).thenReturn(Futures.completed(response3));
        final CompletableFuture<HttpResponse> response33 = interceptor.proceed(request0, next);
        then(response33.isDone()).isTrue();
        then(response33.isCompletedExceptionally()).isFalse();
        then(response33.getNow(null)).isSameAs(response3);
        ctx.clear();

        // Case 4: when complete with location, status is 302
        ctx.maxRedirects(10);
        final HttpResponse response4 = new MockHttpResponse(302);
        response4.headers().add(HttpHeaderNames.LOCATION, "http://127.0.0.1:9999/abc");
        when(next.proceed(request0)).thenReturn(Futures.completed(response4));
        final CompletableFuture<HttpResponse> response44 = interceptor.proceed(request0, next);
        then(response44.isDone()).isTrue();
        then(response44.isCompletedExceptionally()).isTrue();
        then((Integer) ctx.getAttr(HAS_REDIRECTED_COUNT)).isEqualTo(10);
        ctx.clear();

        // Case 5: when complete with location, status is 302 and redirected response is null
        ctx.maxRedirects(10);
        final HttpResponse response5 = new MockHttpResponse(302);
        response5.headers().add(HttpHeaderNames.LOCATION, "127.0.0.1:9999");
        when(next.proceed(request0)).thenReturn(Futures.completed(response5));
        final CompletableFuture<HttpResponse> response55 = interceptor.proceed(request0, next);
        then(response55.isDone()).isTrue();
        then(response55.isCompletedExceptionally()).isTrue();
        then((Integer) ctx.getAttr(HAS_REDIRECTED_COUNT)).isEqualTo(0);
        ctx.clear();
    }

    @Test
    void testNewRequest() {
        final RedirectInterceptor interceptor = new RedirectInterceptor();
        final HttpRequest request0 = client.get("http://127.0.0.1:9999/abc/def");

        final HttpRequest request1 = interceptor.newRequest(request0,
                java.net.URI.create("http://127.0.0.1:8888/abc/def"), 301);
        then(request1.uri().netURI().toString()).isEqualTo("http://127.0.0.1:8888/abc/def");
        then(request1.headers().isEmpty()).isTrue();
        then(request1.paramNames().isEmpty()).isTrue();
    }

    @Test
    void testDetectUri() throws RedirectException {
        final RedirectInterceptor interceptor = new RedirectInterceptor();

        // location is absent
        final HttpRequest request = client.get("http://127.0.0.1:9999/abc");
        final HttpResponse response = new MockHttpResponse(301);
        final MockContext ctx = new MockContext();
        Executable executable = () -> interceptor.detectURI(request, response);
        Assertions.assertThrows(RedirectException.class, executable);
        ctx.clear();

        response.headers().set(HttpHeaderNames.LOCATION, "");
        Assertions.assertThrows(RedirectException.class, executable);
        ctx.clear();
        response.headers().clear();

        response.headers().set(HttpHeaderNames.LOCATION, "/abc/def");
        then(interceptor.detectURI(request, response).toString()).isEqualTo("http://127.0.0.1:9999/abc/def");
        ctx.clear();
        response.headers().clear();

        response.headers().set(HttpHeaderNames.LOCATION, "/abc/def?a=b&c=d#mn");
        then(interceptor.detectURI(request, response).toString())
                .isEqualTo("http://127.0.0.1:9999/abc/def?a=b&c=d#mn");
        ctx.clear();
        response.headers().clear();
    }

    @Test
    void testToRelativeInfo() {
        final RedirectInterceptor interceptor = new RedirectInterceptor();

        final String relative0 = "/abc/def?a=b&c=d#aaa";
        final RedirectInterceptor.RelativeInfo info0 = interceptor.toRelativeInfo(relative0);
        then(info0.path()).isEqualTo("/abc/def");
        then(info0.query()).isEqualTo("a=b&c=d");
        then(info0.fragment()).isEqualTo("aaa");

        final String relative1 = "/abc/def?a=b&c=d";
        final RedirectInterceptor.RelativeInfo info1 = interceptor.toRelativeInfo(relative1);
        then(info1.path()).isEqualTo("/abc/def");
        then(info1.query()).isEqualTo("a=b&c=d");
        then(info1.fragment()).isNull();

        final String relative2 = "/abc/def#aaa";
        final RedirectInterceptor.RelativeInfo info2 = interceptor.toRelativeInfo(relative2);
        then(info2.path()).isEqualTo("/abc/def");
        then(info2.query()).isNull();
        then(info2.fragment()).isEqualTo("aaa");

        final String relative3 = "/abc/def";
        final RedirectInterceptor.RelativeInfo info3 = interceptor.toRelativeInfo(relative3);
        then(info3.path()).isEqualTo("/abc/def");
        then(info3.query()).isNull();
        then(info3.fragment()).isNull();

        final String relative4 = "";
        final RedirectInterceptor.RelativeInfo info4 = interceptor.toRelativeInfo(relative4);
        then(info4.path()).isEqualTo("");
        then(info4.query()).isNull();
        then(info4.fragment()).isNull();

        final String relative5 = "/?#";
        final RedirectInterceptor.RelativeInfo info5 = interceptor.toRelativeInfo(relative5);
        then(info5.path()).isEqualTo("/");
        then(info5.query()).isEqualTo("");
        then(info5.fragment()).isEqualTo("");
    }

    @Test
    void testShouldRedirect() {
        final RedirectInterceptor interceptor = new RedirectInterceptor();

        // 301 and location is present
        final MockHttpResponse response0 = new MockHttpResponse(301);
        response0.headers().add(HttpHeaderNames.LOCATION, "http://127.0.0.1:9999/abc");
        then(interceptor.shouldRedirect(response0)).isTrue();

        // 301 and location is absent
        final MockHttpResponse response1 = new MockHttpResponse(301);
        then(interceptor.shouldRedirect(response1)).isFalse();

        // 302 and location is present
        final MockHttpResponse response2 = new MockHttpResponse(302);
        response2.headers().add(HttpHeaderNames.LOCATION, "http://127.0.0.1:9999/abc");
        then(interceptor.shouldRedirect(response2)).isTrue();

        // 302 and location is absent
        final MockHttpResponse response3 = new MockHttpResponse(302);
        then(interceptor.shouldRedirect(response3)).isFalse();

        // 303 and location is present
        final MockHttpResponse response4 = new MockHttpResponse(303);
        response4.headers().add(HttpHeaderNames.LOCATION, "http://127.0.0.1:9999/abc");
        then(interceptor.shouldRedirect(response4)).isTrue();

        // 303 and location is absent
        final MockHttpResponse response5 = new MockHttpResponse(303);
        then(interceptor.shouldRedirect(response5)).isFalse();

        // 307 and location is present
        final MockHttpResponse response6 = new MockHttpResponse(307);
        response6.headers().add(HttpHeaderNames.LOCATION, "http://127.0.0.1:9999/abc");
        then(interceptor.shouldRedirect(response6)).isTrue();

        // 307 and location is absent
        final MockHttpResponse response7 = new MockHttpResponse(307);
        then(interceptor.shouldRedirect(response7)).isFalse();

        // 308 and location is present
        final MockHttpResponse response8 = new MockHttpResponse(308);
        response8.headers().add(HttpHeaderNames.LOCATION, "http://127.0.0.1:9999/abc");
        then(interceptor.shouldRedirect(response8)).isTrue();

        // 308 and location is absent
        final MockHttpResponse response9 = new MockHttpResponse(308);
        then(interceptor.shouldRedirect(response9)).isFalse();

        // other status and location is present
        final MockHttpResponse response10 = new MockHttpResponse(309);
        response10.headers().add(HttpHeaderNames.LOCATION, "http://127.0.0.1:9999/abc");
        then(interceptor.shouldRedirect(response10)).isFalse();

        // other status and location is present
        final MockHttpResponse response11 = new MockHttpResponse(309);
        then(interceptor.shouldRedirect(response11)).isFalse();
    }

    @Test
    void testStandardHeaders() {
        final HttpHeaders headers = new Http1HeadersImpl();
        headers.add(HttpHeaderNames.HOST, "127.0.0.1");
        headers.add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        headers.add(HttpHeaderNames.CONTENT_LENGTH, 0);
        headers.add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

        final RedirectInterceptor interceptor = new RedirectInterceptor();
        interceptor.standardHeaders(headers, ThreadLocalRandom.current().nextBoolean());
        then(headers.isEmpty()).isTrue();
    }

    @Test
    void testSucceedWhenExhausted() throws Exception {
        final int maxRedirects = 10;
        final AtomicInteger count = new AtomicInteger();

        final MockContext ctx = new MockContext();
        final HttpResponse response = new MockHttpResponse(302);
        response.headers().add(HttpHeaderNames.LOCATION, "/abc");
        final ExecChain chain = mock(ExecChain.class);
        when(chain.ctx()).thenReturn(ctx);
        ctx.maxRedirects(maxRedirects);

        final HttpResponse succeed = new MockHttpResponse(200);
        succeed.headers().add("a", "b");

        when(chain.proceed(any(HttpRequest.class))).thenAnswer(answer -> {
            int count0 = count.getAndIncrement();
            if (count0 < maxRedirects) {
                return Futures.completed(response);
            } else if (count0 == maxRedirects) {
                return Futures.completed(succeed);
            } else {
                throw new RuntimeException();
            }
        });

        final RedirectInterceptor interceptor = new RedirectInterceptor();
        final HttpResponse result = interceptor.proceed(client.get("/abc"), chain).get();
        then(result.status()).isEqualTo(200);
        then(result.headers().get("a")).isEqualTo("b");
        final int hasRedirectedCount = ctx.getAttr(HAS_REDIRECTED_COUNT);
        then(hasRedirectedCount).isEqualTo(maxRedirects);
    }

    private static final class AuxiliaryRedirectInterceptor extends RedirectInterceptor {

        private static final HttpResponse RESPONSE = new MockHttpResponse(200);

        private AuxiliaryRedirectInterceptor() {
        }

        @Override
        protected void doRedirect(CompletableFuture<HttpResponse> response,
                                  HttpRequest request,
                                  ExecChain next,
                                  int maxRedirects) {
            next.ctx().setAttr(DO_REDIRECT, true);
            response.complete(RESPONSE);
        }
    }

    private static final class AuxiliaryRedirectInterceptor1 extends RedirectInterceptor {

        private AuxiliaryRedirectInterceptor1() {
        }

        @Override
        protected HttpRequest newRequest(HttpRequest request, URI uri, int status) {
            return request;
        }
    }

}
