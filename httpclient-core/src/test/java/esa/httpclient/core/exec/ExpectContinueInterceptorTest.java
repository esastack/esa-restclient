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

import esa.commons.http.HttpHeaderNames;
import esa.httpclient.core.ChunkRequest;
import esa.httpclient.core.ContextImpl;
import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.mock.MockHttpResponse;
import esa.httpclient.core.util.Futures;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.junit.jupiter.api.Test;

import static esa.httpclient.core.ContextNames.EXPECT_CONTINUE_ENABLED;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpectContinueInterceptorTest {

    private static final ExpectContinueInterceptor EXPECT_CONTINUE_INTERCEPTOR
            = new ExpectContinueInterceptor();

    @Test
    void testProceed() {
        final ExecChain next = mock(ExecChain.class);
        final ContextImpl ctx = new ContextImpl();
        when(next.ctx()).thenReturn(ctx);

        final HttpClient client = HttpClient.ofDefault();

        final ChunkRequest request0 = client.prepare("http://127.0.0.1:8080/abc").build();
        when(next.proceed(request0)).thenReturn(Futures.completed(new MockHttpResponse()));

        // Case 1: EXPECT_CONTINUE_ENABLED in context is null
        EXPECT_CONTINUE_INTERCEPTOR.proceed(request0, next);
        then(request0.getHeader(HttpHeaderNames.EXPECT)).isNull();
        ctx.clear();

        // Case 2: EXPECT_CONTINUE_ENABLED in context is false
        ctx.setAttr(EXPECT_CONTINUE_ENABLED, false);
        EXPECT_CONTINUE_INTERCEPTOR.proceed(request0, next);
        then(request0.getHeader(HttpHeaderNames.EXPECT)).isNull();

        // Case 3: EXPECT_CONTINUE_ENABLED in context is true(chunked)
        ctx.clear();
        ctx.setAttr(EXPECT_CONTINUE_ENABLED, true);
        EXPECT_CONTINUE_INTERCEPTOR.proceed(request0, next);
        then(request0.getHeader(HttpHeaderNames.EXPECT)).isNull();

        final HttpRequest request1 = HttpRequest.get("http://127.0.0.1:8080/abc").build();
        // Case 4: EXPECT_CONTINUE_ENABLED in context is true(non-chunked, body is empty)
        ctx.clear();
        ctx.setAttr(EXPECT_CONTINUE_ENABLED, true);
        EXPECT_CONTINUE_INTERCEPTOR.proceed(request1, next);
        then(request1.getHeader(HttpHeaderNames.EXPECT)).isNull();

        final HttpRequest request2 = HttpRequest.post("http://127.0.0.1:8080/abc")
                .body("Hello World!".getBytes()).build();
        // Case 4: EXPECT_CONTINUE_ENABLED in context is true(non-chunked, body is empty)
        ctx.clear();
        ctx.setAttr(EXPECT_CONTINUE_ENABLED, true);
        EXPECT_CONTINUE_INTERCEPTOR.proceed(request2, next);
        then(HttpHeaderValues.CONTINUE.toString()).isEqualTo(request2.getHeader(HttpHeaderNames.EXPECT));

        // Case 5: EXPECT_CONTINUE_ENABLED in context is true(non-chunked, body is empty)
        final HttpRequest request3 = HttpRequest.post("http://127.0.0.1:8080/abc")
                .body("Hello World!".getBytes()).build();
        ctx.clear();
        ctx.setAttr(EXPECT_CONTINUE_ENABLED, true);
        request3.setHeader(HttpHeaderNames.EXPECT, "100-continue0");
        EXPECT_CONTINUE_INTERCEPTOR.proceed(request3, next);
        then("100-continue0").isEqualTo(request3.getHeader(HttpHeaderNames.EXPECT));
    }

    @Test
    void testEmptyBody() {
        then(EXPECT_CONTINUE_INTERCEPTOR.emptyBody(HttpRequest.get("/abc").build())).isTrue();
        then(EXPECT_CONTINUE_INTERCEPTOR.emptyBody(HttpRequest.post("/abc").body(null).build())).isTrue();
        then(EXPECT_CONTINUE_INTERCEPTOR.emptyBody(HttpRequest.post("/abc").body(new byte[0]).build())).isTrue();
    }

}
