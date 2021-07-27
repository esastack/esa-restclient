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
package esa.httpclient.core.exec;

import esa.httpclient.core.ExecContextUtil;
import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.mock.MockContext;
import esa.httpclient.core.mock.MockHttpResponse;
import esa.httpclient.core.util.Futures;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LinkedExecChainTest {

    private final HttpClient client = HttpClient.ofDefault();

    @Test
    void testFrom1() {
        final HttpTransceiver transceiver = mock(HttpTransceiver.class);
        final HttpRequest request = client.get("http://127.0.0.1:8888/abc/def");
        final HttpResponse response = new MockHttpResponse(200);

        final ExecContext ctx = ExecContextUtil.from(new MockContext());
        when(transceiver.handle(request, ctx))
                .thenReturn(Futures.completed(response));

        // interceptors.length == 0
        final ExecChain chain1 = LinkedExecChain.from(new Interceptor[0],
                transceiver, ctx);
        final CompletableFuture<HttpResponse> response11 = chain1.proceed(request);
        then(response11.isDone()).isTrue();
        then(response11.getNow(null)).isSameAs(response);
        request.headers().clear();
        response.headers().clear();
        ((MockContext) ctx.ctx()).clear();

        final Interceptor interceptor1 = (request1, next) -> {
            request1.setHeader("requestInterceptor1", "1");
            return next.proceed(request1).thenApply(rsp -> {
                rsp.headers().set("responseInterceptor1", "1");
                return rsp;
            });
        };

        final Interceptor interceptor2 = (request1, next) -> {
            request1.setHeader("requestInterceptor2", "2");
            return next.proceed(request1).thenApply(rsp -> {
                rsp.headers().set("responseInterceptor2", "2");
                return rsp;
            });
        };

        // interceptors.length == 2
        final ExecChain chain2 = LinkedExecChain.from(new Interceptor[]{interceptor1, interceptor2},
                transceiver, ctx);
        final CompletableFuture<HttpResponse> response22 = chain2.proceed(request);
        then(response22.isDone()).isTrue();
        then(response22.getNow(null)).isSameAs(response);
        then(request.headers().get("requestInterceptor1")).isEqualTo("1");
        then(request.headers().get("requestInterceptor2")).isEqualTo("2");
        then(response.headers().get("responseInterceptor1")).isEqualTo("1");
        then(response.headers().get("responseInterceptor2")).isEqualTo("2");
        request.headers().clear();
        response.headers().clear();
        ((MockContext) ctx.ctx()).clear();

        final Interceptor interceptor3 = (request12, next) -> Futures.completed(new RuntimeException());
        final Interceptor interceptor4 = (request13, next) ->
                next.proceed(request13).thenCompose(rsp -> Futures.completed(new IllegalStateException()));

        // throwable in preHandle
        final ExecChain chain3 = LinkedExecChain.from(new Interceptor[]{interceptor1, interceptor3},
                transceiver, ctx);
        final CompletableFuture<HttpResponse> response33 = chain3.proceed(request);
        then(response33.isDone()).isTrue();
        then(response33.isCompletedExceptionally()).isTrue();
        then(request.headers().get("requestInterceptor1")).isEqualTo("1");
        request.headers().clear();
        response.headers().clear();
        ((MockContext) ctx.ctx()).clear();

        // throwable in postHandle
        final ExecChain chain4 = LinkedExecChain.from(new Interceptor[]{interceptor4, interceptor1, interceptor2},
                transceiver,
                ctx);
        final CompletableFuture<HttpResponse> response44 = chain4.proceed(request);
        then(response44.isDone()).isTrue();
        then(response44.isCompletedExceptionally()).isTrue();
        then(request.headers().get("requestInterceptor1")).isEqualTo("1");
        then(request.headers().get("requestInterceptor2")).isEqualTo("2");
        then(response.headers().get("responseInterceptor1")).isEqualTo("1");
        then(response.headers().get("responseInterceptor2")).isEqualTo("2");
        request.headers().clear();
        response.headers().clear();
        ((MockContext) ctx.ctx()).clear();
    }

    @Test
    void testFrom2() {
        final HttpTransceiver transceiver = mock(HttpTransceiver.class);
        final HttpRequest request = client.get("http://127.0.0.1:8888/abc/def");

        final ExecContext ctx = ExecContextUtil.from(new MockContext());
        final HttpResponse response = new MockHttpResponse(200);

        when(transceiver.handle(request, ctx))
                .thenReturn(Futures.completed(response));

        // interceptors.length == 0
        final ExecChain chain1 = LinkedExecChain.from(new Interceptor[0],
                transceiver, ctx);
        final CompletableFuture<HttpResponse> response11 = chain1.proceed(request);
        then(response11.isDone()).isTrue();
        then(response11.getNow(null)).isSameAs(response);
        request.headers().clear();
        response.headers().clear();
        ((MockContext) ctx.ctx()).clear();

        final Interceptor interceptor1 = (request1, next) -> {
            request1.setHeader("requestInterceptor1", "1");
            return next.proceed(request1).thenApply(rsp -> {
                rsp.headers().set("responseInterceptor1", "1");
                return rsp;
            });
        };

        final Interceptor interceptor2 = (request1, next) -> {
            request1.setHeader("requestInterceptor2", "2");
            return next.proceed(request1).thenApply(rsp -> {
                rsp.headers().set("responseInterceptor2", "2");
                return rsp;
            });
        };

        // interceptors.length == 2
        final ExecChain chain2 = LinkedExecChain.from(new Interceptor[]{interceptor1, interceptor2},
                transceiver, ctx);
        final CompletableFuture<HttpResponse> response22 = chain2.proceed(request);
        then(response22.isDone()).isTrue();
        then(response22.getNow(null)).isSameAs(response);
        then(request.headers().get("requestInterceptor1")).isEqualTo("1");
        then(request.headers().get("requestInterceptor2")).isEqualTo("2");
        then(response.headers().get("responseInterceptor1")).isEqualTo("1");
        then(response.headers().get("responseInterceptor2")).isEqualTo("2");
        request.headers().clear();
        response.headers().clear();
        ((MockContext) ctx.ctx()).clear();

        final Interceptor interceptor3 = (request12, next) -> Futures.completed(new RuntimeException());
        final Interceptor interceptor4 = (request13, next) ->
                next.proceed(request13).thenCompose(rsp -> Futures.completed(new IllegalStateException()));

        // throwable in preHandle
        final ExecChain chain3 = LinkedExecChain.from(new Interceptor[]{interceptor1, interceptor3},
                transceiver, ctx);
        final CompletableFuture<HttpResponse> response33 = chain3.proceed(request);
        then(response33.isDone()).isTrue();
        then(response33.isCompletedExceptionally()).isTrue();
        then(request.headers().get("requestInterceptor1")).isEqualTo("1");
        request.headers().clear();
        response.headers().clear();
        ((MockContext) ctx.ctx()).clear();

        // throwable in postHandle
        final ExecChain chain4 = LinkedExecChain.from(new Interceptor[]{interceptor4, interceptor1, interceptor2},
                transceiver, ctx);
        final CompletableFuture<HttpResponse> response44 = chain4.proceed(request);
        then(response44.isDone()).isTrue();
        then(response44.isCompletedExceptionally()).isTrue();
        then(request.headers().get("requestInterceptor1")).isEqualTo("1");
        then(request.headers().get("requestInterceptor2")).isEqualTo("2");
        then(response.headers().get("responseInterceptor1")).isEqualTo("1");
        then(response.headers().get("responseInterceptor2")).isEqualTo("2");
        request.headers().clear();
        response.headers().clear();
        ((MockContext) ctx.ctx()).clear();
    }

}
