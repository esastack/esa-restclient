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

import io.esastack.commons.net.http.HttpStatus;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.filter.RequestFilter;
import io.esastack.httpclient.core.mock.MockHttpResponse;
import io.esastack.httpclient.core.util.Futures;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilteringExecTest {

    private final HttpClient client = HttpClient.ofDefault();

    @Test
    void testProceed() {
        final HttpRequest request = client.get("http://127.0.0.1:9999/abc/def");
        final Context ctx = new Context();
        final HttpResponse response = new MockHttpResponse(HttpStatus.OK.code());
        final ExecChain chain = mock(ExecChain.class);
        when(chain.proceed(request)).thenReturn(Futures.completed(response));
        when(chain.ctx()).thenReturn(ctx);

        // Case 1: request filters are absent
        final FilteringExec exec1 = new FilteringExec(null);
        then(exec1.proceed(request, chain).getNow(null)).isSameAs(response);
        final FilteringExec exec2 = new FilteringExec(new RequestFilter[0]);
        then(exec2.proceed(request, chain).getNow(null)).isSameAs(response);

        final RequestFilter requestFilter1 = (request1, ctx1) -> {
            request1.setHeader("requestFilter1", "1");
            return Futures.completed();
        };

        final RequestFilter requestFilter2 = (request1, ctx1) -> {
            request1.setHeader("requestFilter2", "2");
            return Futures.completed();
        };

        // Case 2: request filters are absent
        request.headers().clear();
        final FilteringExec exec3 = new FilteringExec(new RequestFilter[]{requestFilter1, requestFilter2});
        final CompletableFuture<HttpResponse> response33 = exec3.proceed(request, chain);
        then(response33.isDone()).isTrue();
        then(response33.getNow(null)).isSameAs(response);
        then(request.headers().get("requestFilter1")).isEqualTo("1");
        then(request.headers().get("requestFilter2")).isEqualTo("2");
        response.headers().clear();
        request.headers().clear();

        // Case 3: exceptions was thrown in request filters
        final FilteringExec exec4 = new FilteringExec(new RequestFilter[]{(request12, ctx13) ->
                Futures.completed(new RuntimeException())});
        final CompletableFuture<HttpResponse> response44 = exec4.proceed(request, chain);
        then(response44.isDone()).isTrue();
        then(response44.isCompletedExceptionally()).isTrue();
    }
}
