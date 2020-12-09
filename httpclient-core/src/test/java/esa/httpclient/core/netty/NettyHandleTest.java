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
package esa.httpclient.core.netty;

import esa.commons.http.HttpVersion;
import esa.commons.netty.core.Buffer;
import esa.commons.netty.core.Buffers;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.httpclient.core.Context;
import esa.httpclient.core.ContextImpl;
import esa.httpclient.core.Handle;
import esa.httpclient.core.Handler;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.NoopListener;
import esa.httpclient.core.util.Futures;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class NettyHandleTest {

    @SuppressWarnings("unchecked")
    @Test
    void testConstructor() {
        final HttpRequest request = mock(HttpRequest.class);
        final Context ctx = mock(Context.class);
        final Listener listener = mock(Listener.class);
        final CompletableFuture<HttpResponse> response = mock(CompletableFuture.class);
        final Consumer<Handle> handle = (h) -> mock(Handle.class);
        final Handler handler = new Handler() {
            @Override
            public void onData(Buffer content) {

            }

            @Override
            public void onEnd() {

            }

            @Override
            public void onError(Throwable cause) {

            }
        };

        assertThrows(NullPointerException.class, () -> new NettyHandle(null, ctx, listener, response));
        assertThrows(NullPointerException.class, () -> new NettyHandle(request, null, listener, response));
        assertThrows(NullPointerException.class, () -> new NettyHandle(request, ctx, null, response));
        assertThrows(NullPointerException.class, () -> new NettyHandle(request, ctx, listener, null));
        new NettyHandle(request, ctx, listener, response);

        assertThrows(NullPointerException.class, () ->
                new NettyHandle((Handler) null, request, ctx, listener, response));
        assertThrows(NullPointerException.class, () ->
                new NettyHandle(handler, null, ctx, listener, response));
        assertThrows(NullPointerException.class, () ->
                new NettyHandle(handler, request, null, listener, response));
        assertThrows(NullPointerException.class, () ->
                new NettyHandle(handler, request, ctx, null, response));
        assertThrows(NullPointerException.class, () ->
                new NettyHandle(handler, request, ctx, listener, null));
        new NettyHandle(handler, request, ctx, listener, response);

        assertThrows(NullPointerException.class, () ->
                new NettyHandle((Consumer<Handle>) null, request, ctx, listener, response));
        assertThrows(NullPointerException.class, () ->
                new NettyHandle(handle, null, ctx, listener, response));
        assertThrows(NullPointerException.class, () ->
                new NettyHandle(handle, request, null, listener, response));
        assertThrows(NullPointerException.class, () ->
                new NettyHandle(handle, request, ctx, null, response));
        assertThrows(NullPointerException.class, () ->
                new NettyHandle(handle, request, ctx, listener, null));
        new NettyHandle(handle, request, ctx, listener, response);
    }

    @Test
    void testOnOpsNormal() {
        final AtomicInteger count = new AtomicInteger();

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new NettyHandle(request, ctx, listener, response);
        handle.onStart((v) -> {});
        handle.onData((b) -> {});
        handle.onTrailer((t) -> {});
        handle.onEnd(v -> {});
        handle.onError(v -> count.incrementAndGet());

        handle.onMessage(null);
        handle.onData((Buffer) null);
        handle.onTrailers(null);
        handle.onEnd();
        handle.onError(new IOException());

        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isFalse();
        then(count.get()).isEqualTo(0);
    }

    @Test
    void testOnStartError() {
        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final AtomicInteger error = new AtomicInteger();

        final NettyHandle handle = new NettyHandle(request, ctx, listener, response);
        handle.onStart((v) -> {
            throw new IllegalArgumentException();
        });
        handle.onError(v -> error.incrementAndGet());

        handle.onMessage(null);

        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(IllegalArgumentException.class);
        then(error.get()).isEqualTo(1);
    }

    @Test
    void testOnEndError() {
        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final AtomicInteger error = new AtomicInteger();

        final NettyHandle handle = new NettyHandle(request, ctx, listener, response);
        handle.onEnd((v) -> {
            throw new IllegalArgumentException();
        });
        handle.onError(v -> error.incrementAndGet());

        handle.onEnd();
        handle.onError(new IOException());

        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(IllegalArgumentException.class);
        then(error.get()).isEqualTo(1);
    }

    @Test
    void testOnDataError() {
        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final AtomicInteger error = new AtomicInteger();

        final NettyHandle handle = new NettyHandle(request, ctx, listener, response);
        handle.onData((v) -> {
            throw new IllegalArgumentException();
        });
        handle.onError(v -> error.incrementAndGet());

        handle.onData((Buffer) null);
        handle.onError(new IOException());

        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(IllegalArgumentException.class);
        then(error.get()).isEqualTo(1);
    }

    @Test
    void testOnTrailerError() {
        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final AtomicInteger error = new AtomicInteger();

        final NettyHandle handle = new NettyHandle(request, ctx, listener, response);
        handle.onTrailer((v) -> {
            throw new IllegalArgumentException();
        });
        handle.onError(v -> error.incrementAndGet());

        handle.onTrailers(null);
        handle.onError(new IOException());

        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(IllegalArgumentException.class);
        then(error.get()).isEqualTo(1);
    }

    @Test
    void testOnErrorError() {
        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new NettyHandle(request, ctx, listener, response);
        handle.onError((v) -> {
            throw new IllegalArgumentException();
        });

        handle.onTrailers(null);
        assertThrows(IllegalArgumentException.class, () -> handle.onError(new IOException()));

        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(IOException.class);
    }

    @Test
    void testNoOpsAfterOnError() {
        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final AtomicInteger count = new AtomicInteger();

        final NettyHandle handle = new NettyHandle(request, ctx, listener, response);
        handle.onStart(v -> count.incrementAndGet())
                .onData(v -> count.incrementAndGet())
                .onTrailer(v -> count.incrementAndGet())
                .onEnd(v -> count.incrementAndGet());

        handle.onError(new RuntimeException());
        handle.onMessage(new HttpMessageImpl(200, HttpVersion.HTTP_1_1, new Http1HeadersImpl()));
        handle.onData(Buffers.buffer("Hello".getBytes()));
        handle.onTrailers(new Http1HeadersImpl());
        handle.onEnd();

        then(count.intValue()).isEqualTo(0);
        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testNoOpsAfterOnEnd() {
        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final AtomicInteger count = new AtomicInteger();

        final NettyHandle handle = new NettyHandle(request, ctx, listener, response);
        handle.onStart(v -> count.incrementAndGet())
                .onData(v -> count.incrementAndGet())
                .onTrailer(v -> count.incrementAndGet())
                .onError(v -> count.incrementAndGet());

        handle.onEnd();

        handle.onMessage(new HttpMessageImpl(200, HttpVersion.HTTP_1_1, new Http1HeadersImpl()));
        handle.onData(Buffers.buffer("Hello".getBytes()));
        handle.onTrailers(new Http1HeadersImpl());
        handle.onError(new RuntimeException());

        then(count.intValue()).isEqualTo(0);
        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isFalse();
    }

}
