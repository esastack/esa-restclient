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

import esa.httpclient.core.Context;
import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.mock.MockContext;
import esa.httpclient.core.mock.MockHttpResponse;
import esa.httpclient.core.util.Futures;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntToLongFunction;

import static esa.httpclient.core.exec.RetryInterceptor.HAS_RETRIED_COUNT;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetryInterceptorTest {

    private static final String DO_RETRY = "$doRetry";

    private final HttpClient client = HttpClient.ofDefault();

    @Test
    void testProceed() {
        // Retry is allowed as default
        final HttpRequest request = client.get("http://127.0.0.1:9999/abc/def");
        final ExecChain chain = mock(ExecChain.class);
        final MockContext ctx = new MockContext();
        final HttpResponse response = new MockHttpResponse(200);
        when(chain.proceed(request)).thenReturn(Futures.completed(response));
        when(chain.ctx()).thenReturn(ctx);
        ctx.maxRetries(10);
        final RetryInterceptor interceptor = new AuxiliaryRetryInterceptor(
                RetryPredicateImpl.DEFAULT, null);
        final CompletableFuture<HttpResponse> response0 = interceptor.proceed(request, chain);
        then(response0.isDone()).isTrue();
        then(response0.getNow(null)).isSameAs(AuxiliaryRetryInterceptor.RESPONSE);
        then((Boolean) ctx.getAttr(DO_RETRY)).isEqualTo(true);
        ctx.clear();

        // Disable retry
        ctx.maxRetries(0);
        final CompletableFuture<HttpResponse> response1 = interceptor.proceed(request, chain);
        then(response1.isDone()).isTrue();
        then(response1.getNow(null)).isSameAs(response);
        then((Boolean) ctx.getAttr(DO_RETRY)).isNull();
        ctx.clear();

        // Retry is not allowed for chunk request
        ctx.maxRetries(10);
        final HttpRequest request1 = client.get("http://127.0.0.1:9999/abc/def").segment();
        when(chain.proceed(request1)).thenReturn(Futures.completed(response));
        final CompletableFuture<HttpResponse> response2 = interceptor.proceed(request1, chain);
        then(response2.isDone()).isTrue();
        then(response2.getNow(null)).isSameAs(response);
        then((Boolean) ctx.getAttr(DO_RETRY)).isNull();
        ctx.clear();
    }

    @Test
    void testDoRetry() {
        final HttpRequest request = client.get("http://127.0.0.1:9999/abc/def");
        final ExecChain chain = mock(ExecChain.class);
        final MockContext ctx = new MockContext();
        when(chain.proceed(request)).thenReturn(Futures.completed(new ConnectException()));
        when(chain.ctx()).thenReturn(ctx);

        final CompletableFuture<HttpResponse> response00 = new CompletableFuture<>();
        final RetryInterceptor interceptor = new RetryInterceptor(RetryPredicateImpl.DEFAULT,
                null);
        interceptor.doRetry(response00, request, chain, 2);
        then(response00.isDone()).isTrue();
        then((Integer) ctx.getAttr(HAS_RETRIED_COUNT)).isEqualTo(2);
        then(response00.isCompletedExceptionally()).isTrue();
        ctx.clear();

        final HttpResponse response1 = new MockHttpResponse(200);
        when(chain.proceed(request)).thenReturn(Futures.completed(new ConnectException()))
                .thenReturn(Futures.completed(response1));
        final CompletableFuture<HttpResponse> response11 = new CompletableFuture<>();
        interceptor.doRetry(response11, request, chain, 2);
        then(response11.isDone()).isTrue();
        then((Integer) ctx.getAttr(HAS_RETRIED_COUNT)).isEqualTo(1);
        then(response11.isCompletedExceptionally()).isFalse();
        then(response11.getNow(null)).isSameAs(response1);
    }

    @Test
    void testSucceedWhenExhausted() throws Exception {
        final int maxRetries = 10;
        final AtomicInteger count = new AtomicInteger();

        final MockContext ctx = new MockContext();
        final ExecChain chain = mock(ExecChain.class);
        when(chain.ctx()).thenReturn(ctx);
        ctx.maxRetries(maxRetries);

        final HttpResponse succeed = new MockHttpResponse(200);

        when(chain.proceed(any(HttpRequest.class))).thenAnswer(answer -> {
            int count0 = count.getAndIncrement();
            if (count0 < maxRetries) {
                return Futures.completed(new ConnectException());
            } else if (count0 == maxRetries) {
                return Futures.completed(succeed);
            } else {
                throw new RuntimeException();
            }
        });

        final RetryInterceptor interceptor = new RetryInterceptor(RetryPredicateImpl.DEFAULT, (cunt) -> 0);
        final HttpResponse result = interceptor.proceed(client.get("/abc"), chain).get();
        then(result.status()).isEqualTo(200);
        int hasRetriedCount = ctx.getAttr(HAS_RETRIED_COUNT);
        then(hasRetriedCount).isEqualTo(maxRetries);
    }

    @Test
    void testBackOff() {
        final HttpRequest request = client.get("http://127.0.0.1:9999/abc/def");
        final ExecChain chain = mock(ExecChain.class);
        final Context ctx = new Context();
        when(chain.proceed(request)).thenReturn(Futures.completed(new ConnectException()));
        when(chain.ctx()).thenReturn(ctx);

        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final List<Long> backOffs = new ArrayList<>(10);
        final IntToLongFunction intervalMs = (count) -> 1000 * count;

        final RetryInterceptor interceptor = new RetryInterceptor(RetryPredicateImpl.DEFAULT, intervalMs) {
            @Override
            protected void backOff(HttpRequest request, int retryCount, IntToLongFunction intervalMs) {
                backOffs.add(intervalMs.applyAsLong(retryCount));
            }
        };

        interceptor.doRetry(response, request, chain, 10);
        then(response.isDone()).isTrue();
        then((Integer) ctx.getAttr(HAS_RETRIED_COUNT)).isEqualTo(10);
        then(response.isCompletedExceptionally()).isTrue();
        for (int i = 0; i < 10; i++) {
            then(backOffs.get(i)).isEqualTo(intervalMs.applyAsLong(i + 1));
        }
    }

    private static final class AuxiliaryRetryInterceptor extends RetryInterceptor {

        private static final HttpResponse RESPONSE = new MockHttpResponse(200);

        private AuxiliaryRetryInterceptor(RetryPredicate predicate, IntToLongFunction intervalMs) {
            super(predicate, intervalMs);
        }

        @Override
        protected void doRetry(CompletableFuture<HttpResponse> response,
                               HttpRequest request,
                               ExecChain next,
                               int maxRetries) {
            next.ctx().setAttr(DO_RETRY, true);
            response.complete(RESPONSE);
        }
    }

}
