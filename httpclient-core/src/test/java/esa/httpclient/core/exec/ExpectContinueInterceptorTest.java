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
import esa.commons.netty.core.Buffer;
import esa.commons.netty.core.BufferImpl;
import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.SegmentRequest;
import esa.httpclient.core.mock.MockContext;
import esa.httpclient.core.mock.MockHttpResponse;
import esa.httpclient.core.util.Futures;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpectContinueInterceptorTest {

    private static final ExpectContinueInterceptor EXPECT_CONTINUE_INTERCEPTOR
            = new ExpectContinueInterceptor();

    @Test
    void testProceed() {
        final ExecChain next = mock(ExecChain.class);
        final MockContext ctx = new MockContext();

        when(next.ctx()).thenReturn(ctx);

        final HttpClient client = HttpClient.ofDefault();

        final SegmentRequest request0 = client.get("http://127.0.0.1:8080/abc").segment();
        when(next.proceed(request0)).thenReturn(Futures.completed(new MockHttpResponse()));

        ctx.useExpectContinue(false);
        // Case 1: disable expect-continue
        EXPECT_CONTINUE_INTERCEPTOR.proceed(request0, next);
        then(request0.getHeader(HttpHeaderNames.EXPECT)).isNull();

        // Case 2: enable expect-continue
        ctx.useExpectContinue(true);
        request0.headers().set(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE);
        EXPECT_CONTINUE_INTERCEPTOR.proceed(request0, next);
        then(request0.getHeader(HttpHeaderNames.EXPECT)).isNull();

        ctx.clear();
        final HttpRequest request1 = client.get("http://127.0.0.1:8080/abc");
        ctx.useExpectContinue(true);

        // Case 3: enable expect-continue but body is empty
        request1.headers().set(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE);
        EXPECT_CONTINUE_INTERCEPTOR.proceed(request1, next);
        then(request1.getHeader(HttpHeaderNames.EXPECT)).isNull();

        final HttpRequest request2 = client.post("http://127.0.0.1:8080/abc")
                .body(new BufferImpl().writeBytes("Hello World!".getBytes()));
        ctx.useExpectContinue(true);
        // Case 4: enable expect-continue but body isn't empty
        EXPECT_CONTINUE_INTERCEPTOR.proceed(request2, next);
        then(HttpHeaderValues.CONTINUE.toString()).isEqualTo(request2.getHeader(HttpHeaderNames.EXPECT));

        // Case 5: disable expect-continue and body isn't empty
        final HttpRequest request3 = client.post("http://127.0.0.1:8080/abc")
                .body(new BufferImpl().writeBytes("Hello World!".getBytes()));
        ctx.useExpectContinue(false);
        EXPECT_CONTINUE_INTERCEPTOR.proceed(request3, next);
        then(request3.getHeader(HttpHeaderNames.EXPECT)).isNull();

        // Case 6: enable expect-continue but body isn't empty
        final HttpRequest request4 = client.post("http://127.0.0.1:8080/abc")
                .body(new BufferImpl().writeBytes("Hello World!".getBytes()));
        ctx.clear();
        ctx.useExpectContinue(false);
        request4.setHeader(HttpHeaderNames.EXPECT, "100-continue0");
        EXPECT_CONTINUE_INTERCEPTOR.proceed(request4, next);
        then("100-continue0").isEqualTo(request4.getHeader(HttpHeaderNames.EXPECT));
    }

    @Test
    void testEmptyBody() {
        final HttpClient client = HttpClient.ofDefault();
        then(EXPECT_CONTINUE_INTERCEPTOR.emptyBody(client.get("/abc"))).isTrue();
        then(EXPECT_CONTINUE_INTERCEPTOR.emptyBody(client.post("/abc").body((Buffer) null))).isTrue();
        then(EXPECT_CONTINUE_INTERCEPTOR.emptyBody(client.post("/abc").body(new BufferImpl()))).isTrue();
        then(EXPECT_CONTINUE_INTERCEPTOR.emptyBody(client.get("/abc").segment())).isTrue();
        then(EXPECT_CONTINUE_INTERCEPTOR.emptyBody(client.get("/abc").multipart()
                .attr("a", "b"))).isFalse();
        then(EXPECT_CONTINUE_INTERCEPTOR.emptyBody(client.get("/abc").multipart())).isTrue();
        then(EXPECT_CONTINUE_INTERCEPTOR.emptyBody(client.get("/abc").body(new File("/")))).isFalse();
    }

}
