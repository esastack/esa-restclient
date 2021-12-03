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
package io.esastack.httpclient.core.netty;

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.ExecContextUtil;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.HttpMessage;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.NoopListener;
import io.esastack.httpclient.core.exec.ExecContext;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.assertj.core.api.BDDAssertions.then;

class DefaultHandleTest {

    @Test
    void testNoopOnXxx() {
        final DefaultHandle0 handle = new DefaultHandle0();

        final Consumer<Void> start = (v) -> {};
        final Consumer<Buffer> data = (d) -> {};
        final Consumer<HttpHeaders> trailer = (t) -> {};
        final Consumer<Void> end = (v) -> {};
        final Consumer<Throwable> error = (th) -> {};

        handle.onStart(start)
                .onData(data)
                .onTrailer(trailer)
                .onEnd(end)
                .onError(error);

        then(handle.start()).isNotSameAs(start);
        then(handle.data()).isNotSameAs(data);
        then(handle.trailer()).isNotSameAs(trailer);
        then(handle.end()).isNotSameAs(end);
        then(handle.error()).isNotSameAs(error);
    }

    @Test
    void testAggregate() {
        final HttpRequest request = HttpClient.ofDefault().get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(NoopListener.INSTANCE);
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final HandleImpl handle1 = new DefaultHandle(ByteBufAllocator.DEFAULT);
        final ResponseHandle nHandle1 = new ResponseHandle(handle1, request, ctx, tHandle, response);
        final HttpMessage message1 = new HttpMessageImpl(202, HttpVersion.HTTP_1_1, new Http1HeadersImpl());

        nHandle1.onMessage(message1);
        nHandle1.onEnd();
        then(handle1.body().readableBytes()).isEqualTo(0);
        then(handle1.headers().isEmpty()).isTrue();
        then(handle1.status()).isEqualTo(202);

        final HandleImpl handle2 = new DefaultHandle(ByteBufAllocator.DEFAULT);
        final ResponseHandle nHandle2 = new ResponseHandle(handle2, request, ctx, tHandle, response);
        final HttpMessage message2 = new HttpMessageImpl(302, HttpVersion.HTTP_1_1, new Http1HeadersImpl());
        message2.headers().add("A", "B");

        final byte[] data = "Hello World!".getBytes();
        nHandle2.onMessage(message2);
        nHandle2.onData(Buffer.defaultAlloc().buffer().writeBytes(data));
        nHandle2.onData(Buffer.defaultAlloc().buffer().writeBytes(data));
        nHandle2.onData(Buffer.defaultAlloc().buffer().writeBytes(data));

        final HttpHeaders trailers = new Http1HeadersImpl();
        trailers.add("D", "E");
        nHandle2.onTrailers(trailers);

        nHandle2.onEnd();
        then(handle2.body().readableBytes()).isEqualTo(data.length * 3);
        then(handle2.headers().get("A")).isEqualTo("B");
        then(handle2.headers().get("D")).isNull();
        then(handle2.status()).isEqualTo(302);
        then(handle2.trailers().get("A")).isNull();
        then(handle2.trailers().get("D")).isEqualTo("E");
    }

    private static final class DefaultHandle0 extends DefaultHandle {

        private DefaultHandle0() {
            super(ByteBufAllocator.DEFAULT);
        }

        private Consumer<Void> start() {
            return super.start;
        }

        private Consumer<Buffer> data() {
            return super.data;
        }

        private Consumer<HttpHeaders> trailer() {
            return super.trailers;
        }

        private Consumer<Void> end() {
            return super.end;
        }

        private Consumer<Throwable> error() {
            return super.error;
        }
    }

}
