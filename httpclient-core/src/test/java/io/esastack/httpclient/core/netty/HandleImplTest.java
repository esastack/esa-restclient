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
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.Handle;
import io.esastack.httpclient.core.Handler;
import io.esastack.httpclient.core.HttpMessage;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HandleImplTest {

    @SuppressWarnings("unchecked")
    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new HandleImpl(null));

        assertThrows(NullPointerException.class, () -> new HandleImpl(null, mock(Handler.class)));
        assertThrows(NullPointerException.class, () -> new HandleImpl(mock(NettyResponse.class), (Handler) null));

        assertThrows(NullPointerException.class, () -> new HandleImpl(null, mock(Consumer.class)));
        assertThrows(NullPointerException.class, () -> new HandleImpl(mock(NettyResponse.class), (Consumer) null));
    }

    @Test
    void testFromHandler() {
        final Handler handler = mock(Handler.class);

        final HandleImpl0 handle = new HandleImpl0(new NettyResponse(false), handler);
        handle.data().accept(null);
        handle.start().accept(null);
        handle.trailers0().accept(null);
        handle.end().accept(null);
        handle.error().accept(null);

        verify(handler).onStart();
        verify(handler).onData(any());
        verify(handler).onTrailers(any());
        verify(handler).onEnd();
        verify(handler).onError(any());
    }

    @Test
    void testFromHandle() {
        final AtomicInteger count = new AtomicInteger();

        final HandleImpl0 handle = new HandleImpl0(new NettyResponse(false), (h) ->
                h.onStart(v -> count.incrementAndGet())
                .onData(b -> count.incrementAndGet())
                .onTrailer(t -> count.incrementAndGet())
                .onEnd(v -> count.incrementAndGet())
                .onError(th -> count.incrementAndGet()));
        handle.data().accept(null);
        handle.start().accept(null);
        handle.trailers0().accept(null);
        handle.end().accept(null);
        handle.error().accept(null);

        then(count.get()).isEqualTo(5);
    }

    @Test
    void testOnOps() {
        final boolean aggregated = ThreadLocalRandom.current().nextBoolean();
        final NettyResponse response = new NettyResponse(aggregated);
        final HandleImpl0 handle = new HandleImpl0(response);

        final Consumer<Void> start = (v) -> {};
        final Consumer<Buffer> data = (d) -> {};
        final Consumer<HttpHeaders> trailers = (t) -> {};
        final Consumer<Void> end = (v) -> {};
        final Consumer<Throwable> error = (th) -> {};

        handle.onStart(start).onData(data).onTrailer(trailers).onError(error).onEnd(end);
        then(handle.start()).isSameAs(start);
        then(handle.data()).isSameAs(data);
        then(handle.trailers0()).isSameAs(trailers);
        then(handle.end()).isSameAs(end);
        then(handle.error()).isSameAs(error);
        then(handle.aggregated()).isEqualTo(aggregated);
    }

    @Test
    void testGetter() {
        final NettyResponse response = new NettyResponse(false);
        final HttpHeaders headers = new Http1HeadersImpl();
        final byte[] data = "Hello World".getBytes();

        final HttpMessage message = new HttpMessageImpl(200, HttpVersion.HTTP_1_1, headers);
        response.message(message);
        response.body(BufferUtil.buffer(data));

        final HandleImpl handle = new HandleImpl(response);
        then(handle.body().readableBytes()).isEqualTo(data.length);
        then(handle.headers()).isSameAs(headers);
        then(handle.status()).isEqualTo(200);
        then(handle.version()).isSameAs(HttpVersion.HTTP_1_1);

        handle.headers().add("a", "b");
        then(response.headers().get("a")).isEqualTo("b");

        then(handle.trailers()).isSameAs(response.trailers());
        then(handle.aggregated()).isFalse();
    }

    private static class HandleImpl0 extends HandleImpl {

        private HandleImpl0(NettyResponse response) {
            super(response);
        }

        private HandleImpl0(NettyResponse response, Handler handler) {
            super(response, handler);
        }

        private HandleImpl0(NettyResponse response, Consumer<Handle> handle0) {
            super(response, handle0);
        }

        private Consumer<Void> start() {
            return super.start;
        }

        private Consumer<Buffer> data() {
            return super.data;
        }

        private Consumer<HttpHeaders> trailers0() {
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
