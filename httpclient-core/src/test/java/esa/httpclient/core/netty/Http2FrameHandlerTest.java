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

import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpVersion;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.Context;
import esa.httpclient.core.ContextImpl;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.NoopListener;
import esa.httpclient.core.exception.ConnectionException;
import esa.httpclient.core.exception.ContentOverSizedException;
import esa.httpclient.core.util.Futures;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DefaultHttp2ConnectionDecoder;
import io.netty.handler.codec.http2.DefaultHttp2ConnectionEncoder;
import io.netty.handler.codec.http2.DefaultHttp2FrameReader;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameReader;
import io.netty.handler.codec.http2.Http2FrameWriter;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import org.junit.jupiter.api.Test;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static esa.httpclient.core.ContextNames.EXPECT_CONTINUE_CALLBACK;
import static io.netty.handler.codec.http2.Http2Error.NO_ERROR;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class Http2FrameHandlerTest {

    private static final byte[] DATA = "Hello World!".getBytes();

    private EmbeddedChannel channel;
    private Http2FrameInboundWriter frameInboundWriter;

    void setUp(HandleRegistry registry, long maxContentLength) {
        final Http2Connection connection = new DefaultHttp2Connection(false);
        final Http2FrameWriter frameWriter = Helper.mockHttp2FrameWriter();
        Http2FrameReader reader = new DefaultHttp2FrameReader();

        Http2ConnectionEncoder encoder = new DefaultHttp2ConnectionEncoder(connection, frameWriter);
        Http2ConnectionDecoder decoder = new DefaultHttp2ConnectionDecoder(connection, encoder, reader);
        final Http2FrameHandler frameListener = new Http2FrameHandler(registry, connection, maxContentLength);
        final Http2ConnectionHandler connectionHandler = new Http2ConnectionHandlerBuilder(registry)
                .codec(decoder, encoder)
                .frameListener(frameListener)
                .build();
        channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        frameInboundWriter = new Http2FrameInboundWriter(channel);
        channel.pipeline().addLast(connectionHandler);
        channel.pipeline().fireChannelActive();

        verify(frameWriter).writeSettings(any(ChannelHandlerContext.class), any(), any(ChannelPromise.class));
        frameInboundWriter.writeInboundSettings(Http2Settings.defaultSettings());
        frameInboundWriter.writeInboundSettingsAck();
        assertNotNull(channel.readOutbound());

        Http2Settings settingsFrame = channel.readOutbound();
        assertNotNull(settingsFrame);

        Object settingAck = channel.readOutbound();
        assertSame(Helper.SETTINGS_ACK, settingAck);
    }

    @Test
    void testHandleOnlyHeadersResponse() throws ExecutionException, InterruptedException {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final long maxContentLength = -1L;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT);
        final int requestId = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.add("x", "y");
        headers.status(HttpResponseStatus.BAD_REQUEST.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, true);

        then(response.isDone()).isTrue();

        final HttpResponse rsp = response.get();
        then(rsp.headers().get("a")).isEqualTo("b");
        then(rsp.headers().get("x")).isEqualTo("y");

        then(rsp.status()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
        then(rsp.body().readableBytes()).isEqualTo(0);
        then(rsp.version()).isEqualTo(HttpVersion.HTTP_2);

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testHandleHeadersAndDataResponse() throws ExecutionException, InterruptedException {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final long maxContentLength = -1L;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT);
        final int requestId = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.add("x", "y");
        headers.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, false);
        frameInboundWriter.writeInboundData(requestId, Unpooled.buffer().writeBytes(DATA), 0,
                true);

        then(response.isDone()).isTrue();

        final HttpResponse rsp = response.get();
        then(rsp.headers().get("a")).isEqualTo("b");
        then(rsp.headers().get("x")).isEqualTo("y");

        then(rsp.status()).isEqualTo(200);
        then(rsp.body().readableBytes()).isEqualTo(DATA.length);
        then(rsp.version()).isEqualTo(HttpVersion.HTTP_2);

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testHandleFullHttpResponse() throws Exception {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final long maxContentLength = -1L;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT);
        final int requestId = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.add("x", "y");
        headers.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, false);
        frameInboundWriter.writeInboundData(requestId, Unpooled.buffer().writeBytes(DATA), 0,
                false);
        final Http2Headers trailers = new DefaultHttp2Headers();
        trailers.add("m", "n");
        trailers.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, trailers, 0, true);

        then(response.isDone()).isTrue();

        final HttpResponse rsp = response.get();
        then(rsp.headers().get("a")).isEqualTo("b");
        then(rsp.headers().get("x")).isEqualTo("y");

        then(rsp.status()).isEqualTo(200);
        then(rsp.trailers().get("m")).isEqualTo("n");
        then(rsp.body().readableBytes()).isEqualTo(DATA.length);
        then(rsp.version()).isEqualTo(HttpVersion.HTTP_2);

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testHandle100ContinueResponse() throws ExecutionException, InterruptedException {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final long maxContentLength = -1L;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final AtomicInteger count = new AtomicInteger();

        ctx.setAttr(EXPECT_CONTINUE_CALLBACK, (Runnable) count::incrementAndGet);

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT);
        final int requestId = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.add("x", "y");
        headers.status(HttpResponseStatus.CONTINUE.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, false);
        then(count.get()).isEqualTo(1);
        then(ctx.getAttr(EXPECT_CONTINUE_CALLBACK)).isNull();

        headers.status(HttpResponseStatus.OK.codeAsText());
        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, false);
        frameInboundWriter.writeInboundData(requestId, Unpooled.buffer().writeBytes(DATA), 0,
                false);
        final Http2Headers trailers = new DefaultHttp2Headers();
        trailers.add("m", "n");
        trailers.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, trailers, 0, true);

        then(response.isDone()).isTrue();

        final HttpResponse rsp = response.get();
        then(rsp.headers().get("a")).isEqualTo("b");
        then(rsp.headers().get("x")).isEqualTo("y");

        then(rsp.status()).isEqualTo(200);
        then(rsp.trailers().get("m")).isEqualTo("n");
        then(rsp.body().readableBytes()).isEqualTo(DATA.length);
        then(rsp.version()).isEqualTo(HttpVersion.HTTP_2);

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testWhenHandleIsNull() {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final long maxContentLength = -1L;

        setUp(registry, maxContentLength);

        final int requestId = 2;

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.add("x", "y");
        headers.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, false);
        frameInboundWriter.writeInboundData(requestId, Unpooled.buffer().writeBytes(DATA),
                0, false);

        channel.finishAndReleaseAll();
    }

    @Test
    void testValidateWithContentLengthHeader1() throws Exception {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final byte[] data = "Hello World!".getBytes();

        // maxContent-Length is -1 (no limit)
        final long maxContentLength1 = -1L;

        setUp(registry, maxContentLength1);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT);
        final int requestId1 = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.add("x", "y");
        headers.add(HttpHeaderNames.CONTENT_LENGTH, "1");
        headers.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId1, headers, 0, false);

        frameInboundWriter.writeInboundData(requestId1, Unpooled.buffer().writeBytes(data), 0,
                false);
        final Http2Headers trailers = new DefaultHttp2Headers();
        trailers.add("m", "n");
        trailers.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId1, trailers, 0, true);

        then(response.isDone()).isTrue();

        final HttpResponse rsp = response.get();
        then(rsp.headers().get("a")).isEqualTo("b");
        then(rsp.headers().get("x")).isEqualTo("y");

        then(rsp.status()).isEqualTo(200);
        then(rsp.trailers().get("m")).isEqualTo("n");
        then(rsp.body().readableBytes()).isEqualTo(data.length);
        then(rsp.version()).isEqualTo(HttpVersion.HTTP_2);

        then(registry.get(requestId1)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testValidateWithContentLengthHeader2() throws Exception {
        final HandleRegistry registry = new HandleRegistry(2, 0);

        // maxContent-Length is configured and contentLength is absent
        final long maxContentLength1 = DATA.length;

        setUp(registry, maxContentLength1);

        final HttpRequest request1 = HttpRequest.get("/abc").build();
        final Context ctx1 = new ContextImpl();
        final Listener listener1 = new NoopListener();
        final CompletableFuture<HttpResponse> response1 = new CompletableFuture<>();

        final NettyHandle handle1 = new DefaultHandle(request1, ctx1, listener1, response1, ByteBufAllocator.DEFAULT);
        final int requestId1 = registry.put(handle1);

        final Http2Headers headers1 = new DefaultHttp2Headers();
        headers1.add("a", "b");
        headers1.add("x", "y");
        headers1.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId1, headers1, 0, false);

        frameInboundWriter.writeInboundData(requestId1, Unpooled.buffer().writeBytes(DATA), 0,
                false);
        final Http2Headers trailers1 = new DefaultHttp2Headers();
        trailers1.add("m", "n");
        trailers1.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId1, trailers1, 0, true);

        then(response1.isDone()).isTrue();

        final HttpResponse rsp1 = response1.get();
        then(rsp1.headers().get("a")).isEqualTo("b");
        then(rsp1.headers().get("x")).isEqualTo("y");

        then(rsp1.status()).isEqualTo(200);
        then(rsp1.trailers().get("m")).isEqualTo("n");
        then(rsp1.body().readableBytes()).isEqualTo(DATA.length);
        then(rsp1.version()).isEqualTo(HttpVersion.HTTP_2);

        then(registry.get(requestId1)).isNull();
        channel.finishAndReleaseAll();


        // data.length > maxContentLength
        final long maxContentLength2 = DATA.length - 1;

        setUp(registry, maxContentLength2);

        final HttpRequest request2 = HttpRequest.get("/abc").build();
        final Context ctx2 = new ContextImpl();
        final Listener listener2 = new NoopListener();
        final CompletableFuture<HttpResponse> response2 = new CompletableFuture<>();

        final NettyHandle handle2 = new DefaultHandle(request2, ctx2, listener2, response2, ByteBufAllocator.DEFAULT);
        final int requestId2 = registry.put(handle2);

        final Http2Headers headers2 = new DefaultHttp2Headers();
        headers2.add("a", "b");
        headers2.add("x", "y");
        headers2.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId2, headers2, 0, false);

        frameInboundWriter.writeInboundData(requestId2, Unpooled.buffer().writeBytes(DATA), 0,
                false);
        final Http2Headers trailers2 = new DefaultHttp2Headers();
        trailers2.add("m", "n");
        trailers2.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId2, trailers2, 0, true);

        then(response2.isDone()).isTrue();
        then(response2.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response2)).isInstanceOf(ContentOverSizedException.class);

        then(registry.get(requestId2)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testValidateWithContentLengthHeader3() {
        final HandleRegistry registry = new HandleRegistry(2, 0);

        // maxContent-Length is configured and contentLength is present (content-length > maxContentLength)
        final long maxContentLength = DATA.length;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT);
        final int requestId = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.add("x", "y");
        headers.add(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(maxContentLength + 1));
        headers.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, false);

        frameInboundWriter.writeInboundData(requestId, Unpooled.buffer().writeBytes(DATA), 0,
                false);
        final Http2Headers trailers = new DefaultHttp2Headers();
        trailers.add("m", "n");
        trailers.status(HttpResponseStatus.OK.codeAsText());

        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(ContentOverSizedException.class);

        then(registry.get(requestId)).isNull();

        frameInboundWriter.writeInboundHeaders(requestId, trailers, 0, true);
        channel.finishAndReleaseAll();
    }

    @Test
    void testValidateWithContentLengthHeader4() {
        final HandleRegistry registry = new HandleRegistry(2, 0);

        // maxContent-Length is configured and contentLength is present (content-length <= maxContentLength)
        final long maxContentLength1 = DATA.length + 1;

        setUp(registry, maxContentLength1);

        final HttpRequest request1 = HttpRequest.get("/abc").build();
        final Context ctx1 = new ContextImpl();
        final Listener listener1 = new NoopListener();
        final CompletableFuture<HttpResponse> response1 = new CompletableFuture<>();

        final NettyHandle handle1 = new DefaultHandle(request1, ctx1, listener1, response1, ByteBufAllocator.DEFAULT);
        final int requestId1 = registry.put(handle1);

        final Http2Headers headers1 = new DefaultHttp2Headers();
        headers1.add("a", "b");
        headers1.add("x", "y");
        headers1.add(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(DATA.length - 1));
        headers1.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId1, headers1, 0, false);

        frameInboundWriter.writeInboundData(requestId1, Unpooled.buffer().writeBytes(DATA), 0,
                false);
        final Http2Headers trailers1 = new DefaultHttp2Headers();
        trailers1.add("m", "n");
        trailers1.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId1, trailers1, 0, true);

        then(response1.isDone()).isTrue();
        then(response1.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response1)).isInstanceOf(ContentOverSizedException.class);
        then(registry.get(requestId1)).isNull();
        channel.finishAndReleaseAll();


        // data.length > maxContentLength
        final long maxContentLength2 = DATA.length - 1;

        setUp(registry, maxContentLength2);

        final HttpRequest request2 = HttpRequest.get("/abc").build();
        final Context ctx2 = new ContextImpl();
        final Listener listener2 = new NoopListener();
        final CompletableFuture<HttpResponse> response2 = new CompletableFuture<>();

        final NettyHandle handle2 = new DefaultHandle(request2, ctx2, listener2, response2, ByteBufAllocator.DEFAULT);
        final int requestId2 = registry.put(handle2);

        final Http2Headers headers2 = new DefaultHttp2Headers();
        headers2.add("a", "b");
        headers2.add("x", "y");
        headers2.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId2, headers2, 0, false);

        frameInboundWriter.writeInboundData(requestId2, Unpooled.buffer().writeBytes(DATA), 0,
                false);
        final Http2Headers trailers2 = new DefaultHttp2Headers();
        trailers2.add("m", "n");
        trailers2.status(HttpResponseStatus.OK.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId2, trailers2, 0, true);

        then(response2.isDone()).isTrue();
        then(response2.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response2)).isInstanceOf(ContentOverSizedException.class);

        then(registry.get(requestId2)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testOnRstStreamRead() {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final long maxContentLength = -1L;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT);
        final int requestId = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.status(HttpResponseStatus.OK.codeAsText());
        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, false);
        frameInboundWriter.writeInboundRstStream(requestId, Http2Error.INTERNAL_ERROR.code());

        then(response.isDone() && response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(Http2Exception.class);
        channel.flush();

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testOnPushPromise() {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final long maxContentLength = -1L;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT);
        final int requestId1 = registry.put(handle);
        final int requestId2 = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.status(HttpResponseStatus.OK.codeAsText());
        frameInboundWriter.writeInboundHeaders(requestId1, headers, 0, false);

        frameInboundWriter.writeInboundPushPromise(requestId1, requestId2,
                new DefaultHttp2Headers(), 0);

        frameInboundWriter.writeInboundData(requestId1, Unpooled.buffer().writeBytes(DATA),
                0, true);

        channel.flush();

        then(response.isDone() && !response.isCompletedExceptionally()).isTrue();
        then(registry.get(requestId1)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testOnStreamRemoved() {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final long maxContentLength = -1L;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT);
        final int requestId = registry.put(handle);

        // The stream will removed when any exception occurred.
        assertThrows(ClosedChannelException.class, () ->
                frameInboundWriter.writeInboundHeaders(requestId,
                        new DefaultHttp2Headers(), 0, true));

        then(response.isDone() && response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(Http2Exception.class);
        channel.flush();

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testOnGoAwayRead() {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final long maxContentLength = -1L;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT);
        final int requestId = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.add("x", "y");
        headers.status(HttpResponseStatus.BAD_REQUEST.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, false);
        frameInboundWriter.writeInboundGoAway(requestId - 2, NO_ERROR.code(), Unpooled.EMPTY_BUFFER);
        channel.flushInbound();

        then(response.isDone() && response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(ConnectionException.class);

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testErrorWhileHandlingOnMessage() {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final long maxContentLength = -1L;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT) {
            @Override
            public void onMessage(esa.httpclient.core.HttpMessage message) {
                throw new IllegalArgumentException();
            }
        };

        final int requestId = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.add("x", "y");
        headers.status(HttpResponseStatus.BAD_REQUEST.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, true);

        then(response.isDone()).isTrue();
        then(registry.get(requestId)).isNull();
        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(Http2Exception.StreamException.class);

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testErrorWhileHandlingOnData() {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final long maxContentLength = -1L;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT) {
            @Override
            public void onData(Buffer content) {
                throw new IllegalArgumentException();
            }
        };

        final int requestId = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.add("x", "y");
        headers.status(HttpResponseStatus.BAD_REQUEST.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, false);
        frameInboundWriter.writeInboundData(requestId, Unpooled.buffer().writeBytes(DATA),
                0, true);

        then(response.isDone()).isTrue();
        then(registry.get(requestId)).isNull();
        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(Http2Exception.StreamException.class);

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testErrorWhileHandingOnTrailers() {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final long maxContentLength = -1L;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT) {

            @Override
            public void onTrailers(esa.commons.http.HttpHeaders trailers) {
                throw new RuntimeException();
            }
        };

        final int requestId = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.add("x", "y");
        headers.status(HttpResponseStatus.BAD_REQUEST.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, false);
        frameInboundWriter.writeInboundData(requestId, Unpooled.EMPTY_BUFFER, 0, false);

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, true);

        then(response.isDone()).isTrue();
        then(registry.get(requestId)).isNull();
        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(Http2Exception.StreamException.class);

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testErrorWhileHandingOnEnd() {
        final HandleRegistry registry = new HandleRegistry(2, 0);
        final long maxContentLength = -1L;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT) {

            @Override
            public void onEnd() {
                throw new IllegalStateException();
            }
        };

        final int requestId = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.add("x", "y");
        headers.status(HttpResponseStatus.BAD_REQUEST.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, false);
        frameInboundWriter.writeInboundData(requestId, Unpooled.EMPTY_BUFFER, 0, false);

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, true);

        then(response.isDone()).isTrue();
        then(registry.get(requestId)).isNull();
        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response)).isInstanceOf(Http2Exception.StreamException.class);

        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

    @Test
    void testErrorWhileHandingOnError() {
        final HandleRegistry registry = new HandleRegistry(2, 0);

        final long maxContentLength = 1L;

        setUp(registry, maxContentLength);

        final HttpRequest request = HttpRequest.get("/abc").build();
        final Context ctx = new ContextImpl();
        final Listener listener = new NoopListener();
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final NettyHandle handle = new DefaultHandle(request, ctx, listener, response, ByteBufAllocator.DEFAULT) {

            @Override
            public void onError(Throwable cause) {
                throw new IllegalArgumentException();
            }
        };
        final int requestId = registry.put(handle);

        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.add("x", "y");
        headers.status(HttpResponseStatus.BAD_REQUEST.codeAsText());

        frameInboundWriter.writeInboundHeaders(requestId, headers, 0, false);
        frameInboundWriter.writeInboundData(requestId, Unpooled.buffer().writeBytes(DATA), 0,
                false);

        assertThrows(ClosedChannelException.class,
                () -> frameInboundWriter.writeInboundHeaders(requestId, headers, 0, true));

        then(response.isDone()).isFalse();
        then(registry.get(requestId)).isNull();
        channel.finishAndReleaseAll();
    }

}
