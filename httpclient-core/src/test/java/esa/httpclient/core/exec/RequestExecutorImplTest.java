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
import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.ListenerProxy;
import esa.httpclient.core.netty.NettyHandle;
import esa.httpclient.core.netty.NettyTransceiver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

import static esa.httpclient.core.ContextNames.EXPECT_CONTINUE_ENABLED;
import static esa.httpclient.core.ContextNames.MAX_REDIRECTS;
import static esa.httpclient.core.ContextNames.MAX_RETRIES;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;

class RequestExecutorImplTest {

    @Test
    @SuppressWarnings("unchecked")
    void testBuild() {
        final RequestExecutorImpl executor = new RequestExecutorImpl(HttpClient.create(),
                new Interceptor[0],
                mock(NettyTransceiver.class),
                10,
                10,
                false);

        final BiFunction<Listener, CompletableFuture<HttpResponse>, NettyHandle> handle = mock(BiFunction.class);
        final ContextImpl ctx = new ContextImpl();
        final Listener listener = ListenerProxy.DEFAULT;

        // redirect: false, retry: false, expectContinue: true
        final HttpRequest request = HttpRequest.get("http://127.0.0.1:8080/abc").maxRedirects(-1)
                .maxRetries(-1).expectContinueEnabled(true).build();

        executor.build(request, handle, ctx, listener, 5000);
        then(ctx.getAttr(EXPECT_CONTINUE_ENABLED)).isEqualTo(true);
        then(ctx.getAttr(MAX_RETRIES)).isEqualTo(10);
        then(ctx.getAttr(MAX_REDIRECTS)).isEqualTo(10);
        ctx.clear();

        final int maxRetries1 = ThreadLocalRandom.current().nextInt(1, 10000);
        final int maxRedirects1 = ThreadLocalRandom.current().nextInt(1, 10000);
        final HttpRequest request1 = HttpRequest.get("http://127.0.0.1:9999/abc")
                .maxRetries(maxRetries1)
                .maxRedirects(maxRedirects1).build();
        executor.build(request1, handle, ctx, listener, 5000);
        then(ctx.getAttr(EXPECT_CONTINUE_ENABLED)).isNull();
        then(ctx.getAttr(MAX_RETRIES)).isEqualTo(maxRetries1);
        then(ctx.getAttr(MAX_REDIRECTS)).isEqualTo(maxRedirects1);
        ctx.clear();

        final HttpRequest request2 = HttpRequest.get("http://127.0.0.1:9999/abc")
                .maxRetries(0)
                .maxRedirects(0).build();
        executor.build(request2, handle, ctx, listener, 5000);
        then(ctx.getAttr(EXPECT_CONTINUE_ENABLED)).isNull();
        then(ctx.getAttr(MAX_RETRIES)).isEqualTo(0);
        then(ctx.getAttr(MAX_REDIRECTS)).isEqualTo(0);
        ctx.clear();
    }

}
