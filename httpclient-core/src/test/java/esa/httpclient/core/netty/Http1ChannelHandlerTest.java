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

import esa.commons.netty.core.Buffer;
import esa.httpclient.core.ExecContextUtil;
import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.exception.ClosedConnectionException;
import esa.httpclient.core.exception.ContentOverSizedException;
import esa.httpclient.core.exception.ProtocolException;
import esa.httpclient.core.exec.ExecContext;
import esa.httpclient.core.util.Futures;
import esa.httpclient.core.util.HttpHeadersUtils;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Http1ChannelHandlerTest {

    private static final byte[] DATA = "Hello World!".getBytes();

    private final HttpClient client = HttpClient.ofDefault();

    @Test
    void testChannelInactive() {
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final Http1ChannelHandler handler = new Http1ChannelHandler(registry, -1L);
        final EmbeddedChannel channel = new EmbeddedChannel(handler);

        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(ctx.listener());
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final ResponseHandle handle = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request, ctx, tHandle, response);
        final int requestId = registry.put(handle);
        handler.updateRequestId(requestId);
        channel.pipeline().fireChannelInactive();

        testChannelInactive0(response, registry, requestId, channel, ClosedConnectionException.class, false);

        channel.finishAndReleaseAll();
    }

    @Test
    void testHandleFullHttpResponse() throws Exception {
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final EmbeddedChannel channel = new EmbeddedChannel();
        final Http1ChannelHandler handler = new Http1ChannelHandler(registry, -1L);
        channel.pipeline().addLast(handler);

        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(ctx.listener());
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final ResponseHandle handle = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request, ctx, tHandle, response);
        final int requestId = registry.put(handle);
        handler.updateRequestId(requestId);
        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("A", "B");
        headers.add("X", "Y");

        channel.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.BAD_REQUEST,
                headers));

        channel.writeInbound(new DefaultHttpContent(Unpooled.buffer().writeBytes(DATA)));

        final LastHttpContent last = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
        last.trailingHeaders().add("M", "N");
        channel.writeInbound(last);

        channel.flushInbound();
        then(response.isDone()).isTrue();

        final HttpResponse rsp = response.get();
        then(rsp.headers().get("A")).isEqualTo("B");
        then(rsp.headers().get("X")).isEqualTo("Y");

        then(rsp.status()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
        then(rsp.trailers().get("M")).isEqualTo("N");
        then(rsp.body().readableBytes()).isEqualTo(DATA.length);
        then(rsp.version()).isEqualTo(esa.commons.http.HttpVersion.HTTP_1_0);

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testHandle100ContinueResponse() throws Exception {
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final EmbeddedChannel channel = new EmbeddedChannel();
        final Http1ChannelHandler handler = new Http1ChannelHandler(registry, -1L);
        channel.pipeline().addLast(handler);

        final HttpRequest request = client.get("/abc");
        final NettyExecContext ctx = ExecContextUtil.newAsNetty();
        final TimeoutHandle tHandle = new TimeoutHandle(ctx.listener());

        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final AtomicInteger count = new AtomicInteger();

        ctx.set100ContinueCallback(count::incrementAndGet);
        final ResponseHandle handle = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request, ctx, tHandle, response);

        final int requestId = registry.put(handle);
        handler.updateRequestId(requestId);

        // Content to be ignored.
        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("A", "B");
        headers.add("X", "Y");

        channel.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.CONTINUE,
                headers));

        channel.writeInbound(new DefaultHttpContent(Unpooled.buffer().writeBytes(DATA)));

        final LastHttpContent last = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
        last.trailingHeaders().add("M", "N");
        channel.writeInbound(last);

        channel.flushInbound();
        then(response.isDone()).isFalse();
        then(registry.get(requestId)).isNotNull();
        then(count.get()).isEqualTo(1);
        then(ctx.remove100ContinueCallback()).isNull();


        // Begin real content
        channel.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.MULTI_STATUS));
        channel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT);
        channel.flushInbound();

        then(response.isDone()).isTrue();
        final HttpResponse rsp = response.get();
        then(rsp.version()).isEqualTo(esa.commons.http.HttpVersion.HTTP_1_0);
        then(rsp.body().readableBytes()).isEqualTo(0);
        then(rsp.headers().size()).isEqualTo(1);
        then(rsp.headers().contains(HttpHeadersUtils.TTFB)).isTrue();
        then(rsp.trailers().isEmpty()).isTrue();
        then(rsp.status()).isEqualTo(HttpResponseStatus.MULTI_STATUS.code());

        channel.finishAndReleaseAll();
    }

    @Test
    void testWhenHandleIsNull() {
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final EmbeddedChannel channel = new EmbeddedChannel();
        final Http1ChannelHandler handler = new Http1ChannelHandler(registry, -1L);
        channel.pipeline().addLast(handler);

        // Current request is 1.
        final int requestId = 1;
        handler.updateRequestId(requestId);

        channel.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        channel.flushInbound();

        channel.checkException();
        then(channel.isActive()).isTrue();

        channel.finishAndReleaseAll();
    }

    @Test
    void testHttpResponseDecodeError() {
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final EmbeddedChannel channel = new EmbeddedChannel();
        final Http1ChannelHandler handler = new Http1ChannelHandler(registry, -1L);
        channel.pipeline().addLast(handler);

        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(ctx.listener());
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final ResponseHandle handle = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request, ctx, tHandle, response);
        final int requestId = registry.put(handle);
        handler.updateRequestId(requestId);
        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("A", "B");
        headers.add("X", "Y");

        final io.netty.handler.codec.http.HttpResponse response0 = new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.BAD_REQUEST,
                headers);
        response0.setDecoderResult(DecoderResult.failure(new IllegalStateException()));

        channel.writeInbound(response0);
        channel.flushInbound();
        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(ProtocolException.class);
        then(registry.get(requestId)).isNull();


        // Content to be ignored.
        channel.writeInbound(new DefaultHttpContent(Unpooled.buffer().writeBytes(DATA)));

        final LastHttpContent last = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
        last.trailingHeaders().add("M", "N");
        channel.writeInbound(last);

        channel.flushInbound();
        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(ProtocolException.class);

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testHttpContentDecodeError() {
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final EmbeddedChannel channel = new EmbeddedChannel();
        final Http1ChannelHandler handler = new Http1ChannelHandler(registry, -1L);
        channel.pipeline().addLast(handler);

        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(ctx.listener());
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final ResponseHandle handle = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request, ctx, tHandle, response);
        // Current request is 1.
        final int requestId = registry.put(handle);
        handler.updateRequestId(requestId);
        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("A", "B");
        headers.add("X", "Y");

        channel.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.BAD_REQUEST,
                headers));

        final HttpContent content = new DefaultHttpContent(Unpooled.buffer().writeBytes(DATA));
        content.setDecoderResult(DecoderResult.failure(new IllegalStateException()));
        channel.writeInbound(content);
        channel.flushInbound();
        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(ProtocolException.class);
        then(registry.get(requestId)).isNull();


        // Content to be ignored
        final LastHttpContent last = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
        last.trailingHeaders().add("M", "N");
        channel.writeInbound(last);
        channel.flushInbound();

        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(ProtocolException.class);

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testValidateWithContentLengthHeader() throws Exception {
        final HandleRegistry registry = new HandleRegistry(1, 0);

        // Case 1: with no contentLength limit (maxContentLength = -1)
        final EmbeddedChannel channel1 = new EmbeddedChannel();
        final Http1ChannelHandler handler1 = new Http1ChannelHandler(registry, -1L);
        channel1.pipeline().addLast(handler1);

        final HttpRequest request1 = client.get("/abc");
        final ExecContext ctx1 = ExecContextUtil.newAs();
        final TimeoutHandle tHandle1 = new TimeoutHandle(ctx1.listener());
        final CompletableFuture<HttpResponse> response1 = new CompletableFuture<>();

        final ResponseHandle handle1 = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request1, ctx1, tHandle1, response1);
        final int requestId1 = registry.put(handle1);
        handler1.updateRequestId(requestId1);
        final HttpHeaders headers1 = new DefaultHttpHeaders();
        headers1.add("A", "B");
        headers1.add("X", "Y");
        headers1.add(HttpHeaderNames.CONTENT_LENGTH, "1");

        channel1.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.BAD_REQUEST,
                headers1));


        channel1.writeInbound(new DefaultHttpContent(Unpooled.buffer().writeBytes(DATA)));

        final LastHttpContent last1 = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
        last1.trailingHeaders().add("M", "N");
        channel1.writeInbound(last1);

        channel1.flushInbound();
        then(response1.isDone()).isTrue();

        final HttpResponse rsp1 = response1.get();
        then(rsp1.headers().get("A")).isEqualTo("B");
        then(rsp1.headers().get("X")).isEqualTo("Y");

        then(rsp1.status()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
        then(rsp1.trailers().get("M")).isEqualTo("N");
        then(rsp1.body().readableBytes()).isEqualTo(DATA.length);
        then(rsp1.version()).isEqualTo(esa.commons.http.HttpVersion.HTTP_1_0);

        then(registry.get(requestId1)).isNull();
        channel1.finishAndReleaseAll();


        // Case 2: content-length is legal  (Content-Length <= maxContentLength)
        final Http1ChannelHandler handler2 = new Http1ChannelHandler(registry, DATA.length);
        final EmbeddedChannel channel2 = new EmbeddedChannel();
        channel2.pipeline().addLast(handler2);

        final HttpRequest request2 = client.get("/abc");
        final ExecContext ctx2 = ExecContextUtil.newAs();
        final TimeoutHandle tHandle2 = new TimeoutHandle(ctx2.listener());
        final CompletableFuture<HttpResponse> response2 = new CompletableFuture<>();

        final ResponseHandle handle2 = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request2, ctx2, tHandle2, response2);
        final int requestId2 = registry.put(handle2);
        handler2.updateRequestId(requestId2);
        final HttpHeaders headers2 = new DefaultHttpHeaders();
        headers2.add("A", "B");
        headers2.add("X", "Y");
        headers2.add(HttpHeaderNames.CONTENT_LENGTH, DATA.length);

        channel2.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.BAD_REQUEST,
                headers2));

        channel2.writeInbound(new DefaultHttpContent(Unpooled.buffer().writeBytes(DATA)));

        final LastHttpContent last2 = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
        last2.trailingHeaders().add("M", "N");
        channel2.writeInbound(last2);

        channel2.flushInbound();
        then(response2.isDone()).isTrue();

        final HttpResponse rsp2 = response2.get();
        then(rsp2.headers().get("A")).isEqualTo("B");
        then(rsp2.headers().get("X")).isEqualTo("Y");

        then(rsp2.status()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
        then(rsp2.trailers().get("M")).isEqualTo("N");
        then(rsp2.body().readableBytes()).isEqualTo(DATA.length);
        then(rsp2.version()).isEqualTo(esa.commons.http.HttpVersion.HTTP_1_0);

        then(registry.get(requestId2)).isNull();
        channel2.finishAndReleaseAll();


        // Case 3: content-length exceeds maxContentLength (Content-Length > maxContentLength)
        final Http1ChannelHandler handler3 = new Http1ChannelHandler(registry, 10L);
        final EmbeddedChannel channel3 = new EmbeddedChannel();
        channel3.pipeline().addLast(handler3);

        final HttpRequest request3 = client.get("/abc");
        final ExecContext ctx3 = ExecContextUtil.newAs();
        final TimeoutHandle tHandle3 = new TimeoutHandle(ctx3.listener());
        final CompletableFuture<HttpResponse> response3 = new CompletableFuture<>();

        final ResponseHandle handle3 = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request3, ctx3, tHandle3, response3);
        final int requestId3 = registry.put(handle3);
        handler3.updateRequestId(requestId3);
        final HttpHeaders headers3 = new DefaultHttpHeaders();
        headers3.add("A", "B");
        headers3.add("X", "Y");
        headers3.add(HttpHeaderNames.CONTENT_LENGTH, "11");

        channel3.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.BAD_REQUEST,
                headers3));
        channel3.flushInbound();

        then(response3.isDone()).isTrue();
        then(response3.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response3)).isInstanceOf(ContentOverSizedException.class);

        then(registry.get(requestId3)).isNull();
        channel3.finishAndReleaseAll();
    }

    @Test
    void testValidateWithoutContentLengthHeader() throws Exception {
        final HandleRegistry registry = new HandleRegistry(1, 0);

        // Case 1: with no contentLength limit (maxContentLength = -1)
        final Http1ChannelHandler handler1 = new Http1ChannelHandler(registry, -1L);
        final EmbeddedChannel channel1 = new EmbeddedChannel();
        channel1.pipeline().addLast(handler1);

        final HttpRequest request1 = client.get("/abc");
        final ExecContext ctx1 = ExecContextUtil.newAs();
        final TimeoutHandle tHandle1 = new TimeoutHandle(ctx1.listener());
        final CompletableFuture<HttpResponse> response1 = new CompletableFuture<>();

        final ResponseHandle handle1 = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request1, ctx1, tHandle1, response1);
        final int requestId1 = registry.put(handle1);
        handler1.updateRequestId(requestId1);
        final HttpHeaders headers1 = new DefaultHttpHeaders();
        headers1.add("A", "B");
        headers1.add("X", "Y");

        channel1.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.BAD_REQUEST,
                headers1));


        channel1.writeInbound(new DefaultHttpContent(Unpooled.buffer().writeBytes(DATA)));

        final LastHttpContent last1 = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
        last1.trailingHeaders().add("M", "N");
        channel1.writeInbound(last1);

        channel1.flushInbound();
        then(response1.isDone()).isTrue();

        final HttpResponse rsp1 = response1.get();
        then(rsp1.headers().get("A")).isEqualTo("B");
        then(rsp1.headers().get("X")).isEqualTo("Y");

        then(rsp1.status()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
        then(rsp1.trailers().get("M")).isEqualTo("N");
        then(rsp1.body().readableBytes()).isEqualTo(DATA.length);
        then(rsp1.version()).isEqualTo(esa.commons.http.HttpVersion.HTTP_1_0);

        then(registry.get(requestId1)).isNull();
        channel1.finishAndReleaseAll();


        // Case 2: content-length is legal  (content.length <= maxContentLength)
        final Http1ChannelHandler handler2 = new Http1ChannelHandler(registry, DATA.length);
        final EmbeddedChannel channel2 = new EmbeddedChannel();
        channel2.pipeline().addLast(handler2);

        final HttpRequest request2 = client.get("/abc");
        final ExecContext ctx2 = ExecContextUtil.newAs();
        final TimeoutHandle tHandle2 = new TimeoutHandle(ctx2.listener());
        final CompletableFuture<HttpResponse> response2 = new CompletableFuture<>();

        final ResponseHandle handle2 = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request2, ctx2, tHandle2, response2);
        final int requestId2 = registry.put(handle2);
        handler2.updateRequestId(requestId2);
        final HttpHeaders headers2 = new DefaultHttpHeaders();
        headers2.add("A", "B");
        headers2.add("X", "Y");

        channel2.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.BAD_REQUEST,
                headers2));

        channel2.writeInbound(new DefaultHttpContent(Unpooled.buffer().writeBytes(DATA)));

        final LastHttpContent last2 = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
        last2.trailingHeaders().add("M", "N");
        channel2.writeInbound(last2);

        channel2.flushInbound();
        then(response2.isDone()).isTrue();

        final HttpResponse rsp2 = response2.get();
        then(rsp2.headers().get("A")).isEqualTo("B");
        then(rsp2.headers().get("X")).isEqualTo("Y");

        then(rsp2.status()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
        then(rsp2.trailers().get("M")).isEqualTo("N");
        then(rsp2.body().readableBytes()).isEqualTo(DATA.length);
        then(rsp2.version()).isEqualTo(esa.commons.http.HttpVersion.HTTP_1_0);

        then(registry.get(requestId2)).isNull();
        channel2.finishAndReleaseAll();


        // Case 3: content-length exceeds maxContentLength (content.length > maxContentLength)
        final Http1ChannelHandler handler3 = new Http1ChannelHandler(registry, DATA.length - 1);
        final EmbeddedChannel channel3 = new EmbeddedChannel();
        channel3.pipeline().addLast(handler3);

        final HttpRequest request3 = client.get("/abc");
        final ExecContext ctx3 = ExecContextUtil.newAs();
        final TimeoutHandle tHandle3 = new TimeoutHandle(ctx3.listener());
        final CompletableFuture<HttpResponse> response3 = new CompletableFuture<>();

        final ResponseHandle handle3 = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request3, ctx3, tHandle3, response3);
        final int requestId3 = registry.put(handle3);
        handler3.updateRequestId(requestId3);
        final HttpHeaders headers3 = new DefaultHttpHeaders();
        headers3.add("A", "B");
        headers3.add("X", "Y");

        channel3.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.BAD_REQUEST,
                headers3));

        channel3.flushInbound();
        channel3.writeInbound(new DefaultHttpContent(Unpooled.buffer().writeBytes(DATA)));
        channel3.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT);
        channel3.flushInbound();

        then(response3.isDone()).isTrue();
        then(response3.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response3)).isInstanceOf(ContentOverSizedException.class);

        then(registry.get(requestId3)).isNull();
        channel3.finishAndReleaseAll();
    }

    @Test
    void testExceptionCaught() {
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final Http1ChannelHandler handler = new Http1ChannelHandler(registry, -1L);
        final EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(handler);

        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(ctx.listener());
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final ResponseHandle handle = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request, ctx, tHandle, response);
        final int requestId = registry.put(handle);
        handler.updateRequestId(requestId);
        channel.pipeline().fireExceptionCaught(new IllegalStateException());

        testChannelInactive0(response, registry, requestId, channel, IllegalStateException.class, true);

        channel.finishAndReleaseAll();
    }

    @Test
    void testErrorWhileHandlingOnMessage() {
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final Http1ChannelHandler handler = new Http1ChannelHandler(registry, -1L);
        final EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(handler);

        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(ctx.listener());
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final ResponseHandle handle = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request, ctx, tHandle, response) {
            @Override
            public void onMessage(esa.httpclient.core.HttpMessage message) {
                throw new IllegalArgumentException();
            }
        };

        final int requestId = registry.put(handle);
        handler.updateRequestId(requestId);
        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("A", "B");
        headers.add("X", "Y");

        channel.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.BAD_REQUEST,
                headers));

        assertThrows(ClosedChannelException.class,
                () -> channel.writeInbound(new DefaultHttpContent(Unpooled.buffer().writeBytes(DATA))));
        assertThrows(ClosedChannelException.class,
                () -> channel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT));

        testChannelInactive0(response, registry, requestId, channel,
                IllegalArgumentException.class, true);
        channel.finishAndReleaseAll();
    }

    @Test
    void testErrorWhileHandlingOnData() {
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final Http1ChannelHandler handler = new Http1ChannelHandler(registry, -1L);
        final EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(handler);

        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(ctx.listener());
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final ResponseHandle handle = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request, ctx, tHandle, response) {
            @Override
            public void onData(Buffer content) {
                throw new IllegalArgumentException();
            }
        };

        final int requestId = registry.put(handle);
        handler.updateRequestId(requestId);
        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("A", "B");
        headers.add("X", "Y");

        channel.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.BAD_REQUEST,
                headers));

        channel.writeInbound(new DefaultHttpContent(Unpooled.buffer().writeBytes(DATA)));
        assertThrows(ClosedChannelException.class,
                () -> channel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT));

        testChannelInactive0(response, registry, requestId, channel,
                IllegalArgumentException.class, true);
        channel.finishAndReleaseAll();
    }

    @Test
    void testErrorWhileHandingOnTrailers() {
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final Http1ChannelHandler handler = new Http1ChannelHandler(registry, -1L);
        final EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(handler);

        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(ctx.listener());
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final ResponseHandle handle = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request, ctx, tHandle, response) {
            @Override
            public void onTrailers(esa.commons.http.HttpHeaders trailers) {
                throw new RuntimeException();
            }
        };

        final int requestId = registry.put(handle);
        handler.updateRequestId(requestId);
        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("A", "B");
        headers.add("X", "Y");

        channel.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.BAD_REQUEST,
                headers));

        channel.writeInbound(new DefaultHttpContent(Unpooled.buffer().writeBytes(DATA)));
        final LastHttpContent last = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
        last.trailingHeaders().add("A", "B");
        channel.writeInbound(last);
        assertThrows(ClosedChannelException.class, channel::flushInbound);

        testChannelInactive0(response, registry, requestId, channel,
                RuntimeException.class, true);
        channel.finishAndReleaseAll();
    }

    @Test
    void testErrorWhileHandingOnEnd() {
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final Http1ChannelHandler handler = new Http1ChannelHandler(registry, -1L);
        final EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(handler);

        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(ctx.listener());
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final ResponseHandle handle = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request, ctx, tHandle, response) {
            @Override
            public void onEnd() {
                throw new IllegalStateException();
            }
        };

        final int requestId = registry.put(handle);
        handler.updateRequestId(requestId);
        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("A", "B");
        headers.add("X", "Y");

        channel.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.BAD_REQUEST,
                headers));

        channel.writeInbound(new DefaultHttpContent(Unpooled.buffer().writeBytes(DATA)));
        final LastHttpContent last = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
        last.trailingHeaders().add("A", "B");
        channel.writeInbound(last);
        assertThrows(ClosedChannelException.class, channel::flushInbound);

        testChannelInactive0(response, registry, requestId, channel,
                IllegalStateException.class, true);
        channel.finishAndReleaseAll();
    }

    @Test
    void testErrorWhileHandingOnError() {
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final Http1ChannelHandler handler = new Http1ChannelHandler(registry, -1L);
        final EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(handler);

        final HttpRequest request = client.get("/abc");
        final ExecContext ctx = ExecContextUtil.newAs();
        final TimeoutHandle tHandle = new TimeoutHandle(ctx.listener());
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final ResponseHandle handle = new ResponseHandle(new DefaultHandle(ByteBufAllocator.DEFAULT),
                request, ctx, tHandle, response) {
            @Override
            public void onError(Throwable cause) {
                throw new IllegalArgumentException();
            }
        };

        final int requestId = registry.put(handle);
        handler.updateRequestId(requestId);
        channel.pipeline().fireExceptionCaught(new IOException());

        then(registry.get(requestId)).isNull();
        then(response.isDone()).isFalse();
        then(channel.isActive()).isFalse();
        then(channel.closeFuture().isDone()).isTrue();

        channel.finishAndReleaseAll();
    }

    private void testChannelInactive0(final CompletableFuture<HttpResponse> response,
                                      HandleRegistry registry,
                                      int requestId,
                                      EmbeddedChannel channel,
                                      Class<? extends Throwable> clazz,
                                      boolean closed) {
        then(registry.get(requestId)).isNull();
        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(clazz);

        if (closed) {
            then(channel.isActive()).isFalse();
            then(channel.closeFuture().isDone()).isTrue();
        }
    }
}
