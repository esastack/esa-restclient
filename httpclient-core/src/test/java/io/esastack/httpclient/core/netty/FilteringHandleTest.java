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
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.ExecContextUtil;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.HttpMessage;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.NoopListener;
import io.esastack.httpclient.core.exec.ExecContext;
import io.esastack.httpclient.core.filter.FilterContext;
import io.esastack.httpclient.core.filter.ResponseFilter;
import io.esastack.httpclient.core.util.Futures;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class FilteringHandleTest {

    private final HttpClient client = HttpClient.ofDefault();

    @SuppressWarnings("unchecked")
    @Test
    void testConstructor() {
        final HttpRequest request = mock(HttpRequest.class);
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(NoopListener.INSTANCE);
        final CompletableFuture<HttpResponse> response = mock(CompletableFuture.class);
        final ResponseFilter[] filters = new ResponseFilter[]{(request1, response1, ctx1) -> null};
        final FilterContext fCtx = mock(FilterContext.class);
        final HandleImpl handle = new DefaultHandle(ByteBufAllocator.DEFAULT);

        assertThrows(NullPointerException.class, () -> new FilteringHandle(null,
                request, ctx, tHandle, response, filters, fCtx));

        assertThrows(NullPointerException.class, () -> new FilteringHandle(handle,
                null, ctx, tHandle, response, filters, fCtx));

        assertThrows(NullPointerException.class, () -> new FilteringHandle(handle,
                request, null, tHandle, response, filters, fCtx));

        assertThrows(NullPointerException.class, () -> new FilteringHandle(handle,
                request, ctx, null, response, filters, fCtx));

        assertThrows(NullPointerException.class, () -> new FilteringHandle(handle,
                request, ctx, tHandle, null, filters, fCtx));

        assertThrows(IllegalArgumentException.class, () -> new FilteringHandle(handle,
                request, ctx, tHandle, response, null, fCtx));

        assertThrows(NullPointerException.class, () -> new FilteringHandle(handle,
                request, ctx, tHandle, response, filters, null));

        assertDoesNotThrow(() -> new FilteringHandle(handle, request, ctx, tHandle, response, filters, fCtx));
    }

    @Test
    void testOpsNormal() throws Exception {
        final HandleImpl handle = new DefaultHandle(ByteBufAllocator.DEFAULT);
        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(NoopListener.INSTANCE);
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final ResponseFilter[] filters = new ResponseFilter[2];

        final CountDownLatch latch = new CountDownLatch(1);
        filters[0] = (request1, response1, ctx1) -> {
            final CompletableFuture<Void> future = new CompletableFuture<>();
            new Thread(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    // Ignore
                }
                response1.headers().add("rspFilter1", "value1");
                future.complete(null);

            }).start();
            return future;
        };

        filters[1] = (request1, response12, ctx12) -> {
            response12.headers().add("rspFilter2", "value2");
            return Futures.completed();
        };

        final FilterContext fCtx = new FilterContext(ctx.ctx());

        final FilteringHandle nHandle = new FilteringHandle(handle, request, ctx,
                tHandle, response, filters, fCtx);

        final HttpHeaders headers = new Http1HeadersImpl();
        headers.add("A", "B");

        final HttpMessage message = new HttpMessageImpl(HttpStatus.OK.code(), HttpVersion.HTTP_1_1, headers);
        nHandle.onMessage(message);

        final byte[] data = new byte[1024 * 1024];
        ThreadLocalRandom.current().nextBytes(data);

        for (int i = 0; i < 1024; i++) {
            nHandle.onData(Buffer.defaultAlloc().buffer(Arrays.copyOfRange(data, 1024 * i, 1024 * (i + 1))));
        }

        final HttpHeaders trailers = new Http1HeadersImpl();
        trailers.add("X", 'Y');
        nHandle.onTrailers(trailers);

        nHandle.onEnd();

        // Ends the response filter now.
        latch.countDown();

        // Waits the response to complete
        final HttpResponse response0 = response.get();
        then(response0.status()).isEqualTo(HttpStatus.OK.code());

        final byte[] bodyData = new byte[1024 * 1024];
        response0.body().readBytes(bodyData);
        then(Arrays.equals(bodyData, data)).isTrue();
        then(response0.headers().get("A")).isEqualTo("B");
        then(response0.headers().get("rspFilter1")).isEqualTo("value1");
        then(response0.headers().get("rspFilter2")).isEqualTo("value2");

        then(response0.trailers().get("X")).isEqualTo("Y");
    }

    @Test
    void testOnFilterError() {
        final HandleImpl handle = new DefaultHandle(ByteBufAllocator.DEFAULT);
        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(NoopListener.INSTANCE);
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final ResponseFilter[] filters = new ResponseFilter[2];

        final CountDownLatch latch = new CountDownLatch(1);

        final RuntimeException ex = new RuntimeException();
        filters[0] = (request1, response1, ctx1) -> {
            final CompletableFuture<Void> future = new CompletableFuture<>();
            new Thread(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    // Ignore
                }
                response1.headers().add("rspFilter1", "value1");
                future.completeExceptionally(ex);
            }).start();
            return future;
        };

        filters[1] = (request2, response12, ctx12) -> {
            response12.headers().add("rspFilter2", "value2");
            return Futures.completed();
        };

        final FilterContext fCtx = new FilterContext(ctx.ctx());

        final FilteringHandle nHandle = new FilteringHandle(handle, request, ctx,
                tHandle, response, filters, fCtx);

        final HttpHeaders headers = new Http1HeadersImpl();
        headers.add("A", "B");

        final HttpMessage message = new HttpMessageImpl(HttpStatus.OK.code(), HttpVersion.HTTP_1_1, headers);
        nHandle.onMessage(message);

        final byte[] data = new byte[1024 * 1024];
        ThreadLocalRandom.current().nextBytes(data);

        for (int i = 0; i < 1024; i++) {
            nHandle.onData(Buffer.defaultAlloc().buffer(Arrays.copyOfRange(data, 1024 * i, 1024 * (i + 1))));
        }

        final HttpHeaders trailers = new Http1HeadersImpl();
        trailers.add("X", 'Y');
        nHandle.onTrailers(trailers);

        nHandle.onEnd();

        // Ends the response filter now.
        latch.countDown();

        // Waits the response to complete
        assertThrows(ExecutionException.class, response::get);
        assertSame(ex, Futures.getCause(response));
    }

    @Test
    void testExceptionThrownByFilter() {
        final HandleImpl handle = new DefaultHandle(ByteBufAllocator.DEFAULT);
        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(NoopListener.INSTANCE);
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final ResponseFilter[] filters = new ResponseFilter[2];

        final CountDownLatch latch = new CountDownLatch(1);

        final RuntimeException ex = new RuntimeException();
        filters[0] = (request1, response1, ctx1) -> {
            throw ex;
        };

        filters[1] = (request12, response12, ctx12) -> {
            response12.headers().add("rspFilter2", "value2");
            return Futures.completed();
        };

        final FilterContext fCtx = new FilterContext(ctx.ctx());
        final FilteringHandle nHandle = new FilteringHandle(handle, request, ctx,
                tHandle, response, filters, fCtx);

        final HttpHeaders headers = new Http1HeadersImpl();
        headers.add("A", "B");

        final HttpMessage message = new HttpMessageImpl(HttpStatus.OK.code(), HttpVersion.HTTP_1_1, headers);
        nHandle.onMessage(message);

        final byte[] data = new byte[1024 * 1024];
        ThreadLocalRandom.current().nextBytes(data);

        for (int i = 0; i < 1024; i++) {
            nHandle.onData(Buffer.defaultAlloc().buffer(Arrays.copyOfRange(data, 1024 * i, 1024 * (i + 1))));
        }

        final HttpHeaders trailers = new Http1HeadersImpl();
        trailers.add("X", 'Y');
        nHandle.onTrailers(trailers);

        nHandle.onEnd();

        // Ends the response filter now.
        latch.countDown();

        // Waits the response to complete
        assertThrows(ExecutionException.class, response::get);
        assertSame(ex, Futures.getCause(response));
    }

    @Test
    void testOnMessageError() {
        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(NoopListener.INSTANCE);
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final HandleImpl handle = new HandleImpl(new NettyResponse(true));

        final RuntimeException ex = new RuntimeException();
        handle.onStart((v) -> {
            throw ex;
        });

        testOnXxxError(request, ctx, tHandle, response, handle, ex);
    }

    @Test
    void testOnDataError() {
        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(NoopListener.INSTANCE);
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final HandleImpl handle = new HandleImpl(new NettyResponse(true));

        final RuntimeException ex = new RuntimeException();
        handle.onData((v) -> {
            throw ex;
        });

        testOnXxxError(request, ctx, tHandle, response, handle, ex);
    }

    @Test
    void testOnTrailersError() {
        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(NoopListener.INSTANCE);
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final HandleImpl handle = new HandleImpl(new NettyResponse(true));

        final RuntimeException ex = new RuntimeException();
        handle.onTrailer((v) -> {
            throw ex;
        });

        testOnXxxError(request, ctx, tHandle, response, handle, ex);
    }

    @Test
    void testOnEndError() {
        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(NoopListener.INSTANCE);

        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final HandleImpl handle = new HandleImpl(new NettyResponse(true));

        final RuntimeException ex = new RuntimeException();
        handle.onEnd((v) -> {
            throw ex;
        });

        testOnXxxError(request, ctx, tHandle, response, handle, ex);
    }

    @Test
    void testOnErrorError() {
        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(NoopListener.INSTANCE);

        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final HandleImpl handle = new HandleImpl(new NettyResponse(true));

        final RuntimeException ex = new IllegalArgumentException();
        handle.onError((v) -> {
            throw new RuntimeException();
        }).onStart((v) -> {
            throw ex;
        });

        testOnXxxError(request, ctx, tHandle, response, handle, ex);
    }

    private void testOnXxxError(HttpRequest request,
                                ExecContext ctx,
                                TimeoutHandle tHandle,
                                CompletableFuture<HttpResponse> response,
                                HandleImpl handle,
                                RuntimeException ex) {
        final ResponseFilter[] filters = new ResponseFilter[2];

        final CountDownLatch latch = new CountDownLatch(1);
        filters[0] = (request1, response1, ctx1) -> {
            final CompletableFuture<Void> future = new CompletableFuture<>();
            new Thread(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    // Ignore
                }
                response1.headers().add("rspFilter1", "value1");
                future.complete(null);

            }).start();
            return future;
        };

        filters[1] = (request12, response12, ctx12) -> {
            response12.headers().add("rspFilter2", "value2");
            return Futures.completed();
        };

        final FilterContext fCtx = new FilterContext(ctx.ctx());
        final FilteringHandle nHandle = new FilteringHandle(handle, request, ctx,
                tHandle, response, filters, fCtx);

        final HttpHeaders headers = new Http1HeadersImpl();
        headers.add("A", "B");
        final HttpMessage message = new HttpMessageImpl(HttpStatus.OK.code(), HttpVersion.HTTP_1_1, headers);
        nHandle.onMessage(message);

        final byte[] data = new byte[1024 * 1024];
        ThreadLocalRandom.current().nextBytes(data);

        for (int i = 0; i < 1024; i++) {
            nHandle.onData(Buffer.defaultAlloc().buffer(Arrays.copyOfRange(data, 1024 * i, 1024 * (i + 1))));
        }

        final HttpHeaders trailers = new Http1HeadersImpl();
        trailers.add("X", 'Y');
        nHandle.onTrailers(trailers);

        nHandle.onEnd();

        // Ends the response filter now.
        latch.countDown();

        // Waits the response to complete
        assertThrows(ExecutionException.class, response::get);
        assertSame(ex, Futures.getCause(response));
    }

}

