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

import esa.httpclient.core.ContextImpl;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.filter.RequestFilter;
import esa.httpclient.core.filter.ResponseFilter;
import esa.httpclient.core.mock.MockHttpResponse;
import esa.httpclient.core.util.Futures;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static esa.httpclient.core.ContextNames.IGNORE_REQUEST_FILTERS;
import static esa.httpclient.core.ContextNames.IGNORE_RESPONSE_FILTERS;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilteringExecTest {

    @Test
    void testProceed() {
        final HttpRequest request = HttpRequest.get("http://127.0.0.1:9999/abc/def").build();
        final ContextImpl ctx = new ContextImpl();
        final HttpResponse response = new MockHttpResponse(200);
        final ExecChain chain = mock(ExecChain.class);
        when(chain.proceed(request)).thenReturn(Futures.completed(response));
        when(chain.ctx()).thenReturn(ctx);

        // Case 1: duplex filters are absent
        final FilteringExec exec1 = new FilteringExec(null, null);
        then(exec1.proceed(request, chain).getNow(null)).isSameAs(response);
        final FilteringExec exec2 = new FilteringExec(new RequestFilter[0], new ResponseFilter[0]);
        then(exec2.proceed(request, chain).getNow(null)).isSameAs(response);


        final RequestFilter requestFilter1 = (request1, ctx1) -> {
            request1.setHeader("requestFilter1", "1");
            return Futures.completed();
        };

        final RequestFilter requestFilter2 = (request1, ctx1) -> {
            request1.setHeader("requestFilter2", "2");
            return Futures.completed();
        };

        final ResponseFilter responseFilter1 = (response1, ctx12) -> {
            response1.headers().set("responseFilter1", "1");
            return Futures.completed();
        };

        final ResponseFilter responseFilter2 = (response1, ctx12) -> {
            response1.headers().set("responseFilter2", "2");
            return Futures.completed();
        };

        // Case 2: request filters are absent
        final FilteringExec exec3 = new FilteringExec(new RequestFilter[0],
                new ResponseFilter[]{responseFilter1, responseFilter2});
        final CompletableFuture<HttpResponse> response22 = exec3.proceed(request, chain);
        then(response22.isDone()).isTrue();
        then(response22.getNow(null)).isSameAs(response);
        then(response.headers().get("responseFilter1")).isEqualTo("1");
        then(response.headers().get("responseFilter2")).isEqualTo("2");
        response.headers().clear();
        request.headers().clear();

        // Case 3: response filters are absent
        final FilteringExec exec4 = new FilteringExec(new RequestFilter[]{requestFilter1, requestFilter2},
                new ResponseFilter[0]);
        final CompletableFuture<HttpResponse> response33 = exec4.proceed(request, chain);
        then(response33.isDone()).isTrue();
        then(response33.getNow(null)).isSameAs(response);
        then(request.headers().get("requestFilter1")).isEqualTo("1");
        then(request.headers().get("requestFilter2")).isEqualTo("2");
        response.headers().clear();
        request.headers().clear();

        final FilteringExec exec5 = new FilteringExec(new RequestFilter[]{requestFilter1, requestFilter2},
                new ResponseFilter[]{responseFilter1, responseFilter2});
        final CompletableFuture<HttpResponse> response44 = exec5.proceed(request, chain);
        then(response44.isDone()).isTrue();
        then(response44.getNow(null)).isSameAs(response);
        then(request.headers().get("requestFilter1")).isEqualTo("1");
        then(request.headers().get("requestFilter2")).isEqualTo("2");
        then(response.headers().get("responseFilter1")).isEqualTo("1");
        then(response.headers().get("responseFilter2")).isEqualTo("2");
        response.headers().clear();
        request.headers().clear();

        // Ignore duplex filters.
        ctx.setAttr(IGNORE_REQUEST_FILTERS, true);
        ctx.setAttr(IGNORE_RESPONSE_FILTERS, true);
        final FilteringExec exec6 = new FilteringExec(new RequestFilter[]{requestFilter1, requestFilter2},
                new ResponseFilter[]{responseFilter1, responseFilter2});
        final CompletableFuture<HttpResponse> response55 = exec6.proceed(request, chain);
        then(response55.isDone()).isTrue();
        then(response55.getNow(null)).isSameAs(response);
        then(request.headers().get("requestFilter1")).isNull();
        then(request.headers().get("requestFilter2")).isNull();
        then(response.headers().get("responseFilter1")).isNull();
        then(response.headers().get("responseFilter2")).isNull();
        response.headers().clear();
        request.headers().clear();
        ctx.clear();

        // Case 4: exceptions was thrown in request filters
        final FilteringExec exec7 = new FilteringExec(new RequestFilter[]{(request12, ctx13) ->
                Futures.completed(new RuntimeException())}, new ResponseFilter[]{responseFilter1, responseFilter2});
        final CompletableFuture<HttpResponse> response66 = exec7.proceed(request, chain);
        then(response66.isDone()).isTrue();
        then(response66.isCompletedExceptionally()).isTrue();
        ctx.clear();

        // Case 5: exception was thrown in response filters
        final FilteringExec exec8 = new FilteringExec(new RequestFilter[]{requestFilter1, requestFilter2},
                new ResponseFilter[]{(response12, ctx14) -> Futures.completed(new RuntimeException())});
        final CompletableFuture<HttpResponse> response77 = exec8.proceed(request, chain);
        then(response77.isDone()).isTrue();
        then(response77.isCompletedExceptionally()).isTrue();
        then(request.headers().get("requestFilter1")).isEqualTo("1");
        then(request.headers().get("requestFilter2")).isEqualTo("2");
        ctx.clear();
    }
}
