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

import esa.commons.netty.core.BufferImpl;
import esa.httpclient.core.Context;
import esa.httpclient.core.ContextImpl;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.NoopListener;
import esa.httpclient.core.exception.ConnectionException;
import esa.httpclient.core.util.Futures;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class Http2ConnectionHandlerTest extends Http2ConnectionHelper {

    @Test
    void testAddChunkWriteHandlerAfterAdded() throws Exception {
        final HandleRegistry registry = new HandleRegistry(2, 1);
        setUp(registry);
        assertNotNull(channel.pipeline().get(ChunkedWriteHandler.class));
        assertSame(channel.pipeline().last(), channel.pipeline().get(ChunkedWriteHandler.class));
    }

    @Test
    void testExceptionCaught() throws Exception {
        final HandleRegistry registry = new HandleRegistry(2, 1);
        setUp(registry);

        final HttpRequest request1 = HttpRequest.get("/abc").build();
        final Context ctx1 = new ContextImpl();
        final Listener listener1 = new NoopListener();
        final CompletableFuture<HttpResponse> response1 = new CompletableFuture<>();

        final NettyHandle handle1 = new DefaultHandle(request1, ctx1, listener1, response1, ByteBufAllocator.DEFAULT);
        final int requestId1 = registry.put(handle1);

        final HttpRequest request2 = HttpRequest.get("/abc").build();
        final Context ctx2 = new ContextImpl();
        final Listener listener2 = new NoopListener();
        final CompletableFuture<HttpResponse> response2 = new CompletableFuture<>();

        final NettyHandle handle2 = new DefaultHandle(request2, ctx2, listener2, response2, ByteBufAllocator.DEFAULT);
        final int requestId2 = registry.put(handle2);

        channel.pipeline().fireExceptionCaught(new IOException());
        then(response1.isDone() && response1.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response1)).isInstanceOf(ConnectionException.class);

        then(response2.isDone() && response2.isCompletedExceptionally()).isTrue();
        then(Futures.getCause(response2)).isInstanceOf(ConnectionException.class);
        then(registry.get(requestId1)).isNull();
        then(registry.get(requestId2)).isNull();

        then(channel.isActive()).isFalse();
        then(channel.closeFuture().isDone()).isTrue();
    }

    @Test
    void testWriteChunkedInput() throws Exception {
        final HandleRegistry registry = new HandleRegistry(2, 1);
        setUp(registry);

        final File file = File.createTempFile("httpclient-", ".tmp");
        file.deleteOnExit();

        try {
            final byte[] data = new byte[1024 * 1024];
            try (FileOutputStream out = new FileOutputStream(file)) {
                ThreadLocalRandom.current().nextBytes(data);
                out.write(data);
            }

            final Http2ChunkedInput chunkedInput =
                    new Http2ChunkedInput(new ChunkedFile(file, 1024),
                            STREAM_ID);
            channel.writeOutbound(chunkedInput);
            assertTrue(channel.finish());
            // ignore h2 preface
            channel.readOutbound();
            final CompositeByteBuf body = Unpooled.compositeBuffer();
            Object frame;
            boolean endStream = false;
            while ((frame = channel.readOutbound()) != null) {
                if (frame instanceof Helper.DataFrame) {
                    assertFalse(endStream);
                    Helper.DataFrame chunk = (Helper.DataFrame) frame;
                    assertEquals(STREAM_ID, chunk.streamId);
                    assertEquals(0, chunk.padding);
                    assertEquals(1024, chunk.data.readableBytes());
                    body.addComponent(true, chunk.data);
                    endStream = chunk.endStream;
                } else {
                    fail();
                }
            }
            assertTrue(endStream);
            assertArrayEquals(data, ByteBufUtil.getBytes(body));
        } finally {
            file.delete();
        }
    }

    @Test
    void testWriteHeaders() throws Exception {
        final HandleRegistry registry = new HandleRegistry(2, 1);
        setUp(registry);

        final Http2ConnectionHandler handler = channel.pipeline().get(Http2ConnectionHandler.class);
        final Http2Headers headers = new DefaultHttp2Headers();
        headers.add("a", "b");
        headers.path("/abc");

        handler.writeHeaders(STREAM_ID, headers, true, channel.newPromise());
        channel.flush();
        channel.readOutbound();
        Object frame = channel.readOutbound();
        then(frame).isInstanceOf(Helper.HeaderFrame.class);
        Helper.HeaderFrame frame0 = (Helper.HeaderFrame) frame;
        then(frame0.endStream).isTrue();
        then(frame0.streamId).isEqualTo(STREAM_ID);
        then(frame0.headers.get("a")).isEqualTo("b");
        then(frame0.headers.path()).isEqualTo("/abc");

        channel.finishAndReleaseAll();
    }

    @Test
    void testWriteArrayData() throws Exception {
        final HandleRegistry registry = new HandleRegistry(2, 1);
        setUp(registry);

        final Http2ConnectionHandler handler = channel.pipeline().get(Http2ConnectionHandler.class);
        final byte[] data = "Hello World".getBytes();
        handler.writeData(STREAM_ID, data, false, channel.newPromise());
        channel.flush();
        channel.readOutbound();
        Object frame = channel.readOutbound();
        then(frame).isInstanceOf(Helper.DataFrame.class);
        Helper.DataFrame frame0 = (Helper.DataFrame) frame;
        then(frame0.endStream).isFalse();
        then(frame0.streamId).isEqualTo(STREAM_ID);
        then(frame0.data.readableBytes()).isEqualTo(data.length);

        channel.finishAndReleaseAll();
    }

    @Test
    void testWriteByteBufData() throws Exception {
        final HandleRegistry registry = new HandleRegistry(2, 1);
        setUp(registry);

        final Http2ConnectionHandler handler = channel.pipeline().get(Http2ConnectionHandler.class);
        final byte[] data = "Hello World".getBytes();

        handler.writeData(STREAM_ID, Unpooled.buffer().writeBytes(data), false, channel.newPromise());
        channel.flush();
        channel.readOutbound();
        Object frame = channel.readOutbound();
        then(frame).isInstanceOf(Helper.DataFrame.class);
        Helper.DataFrame frame0 = (Helper.DataFrame) frame;
        then(frame0.endStream).isFalse();
        then(frame0.streamId).isEqualTo(STREAM_ID);
        then(frame0.data.readableBytes()).isEqualTo(data.length);

        channel.finishAndReleaseAll();
    }

    @Test
    void testWriteBufferData() throws Exception {
        final HandleRegistry registry = new HandleRegistry(2, 1);
        setUp(registry);

        final Http2ConnectionHandler handler = channel.pipeline().get(Http2ConnectionHandler.class);
        final byte[] data = "Hello World".getBytes();

        handler.writeData(STREAM_ID, new BufferImpl(Unpooled.buffer().writeBytes(data)),
                false, channel.newPromise());
        channel.flush();
        channel.readOutbound();
        Object frame = channel.readOutbound();
        then(frame).isInstanceOf(Helper.DataFrame.class);
        Helper.DataFrame frame0 = (Helper.DataFrame) frame;
        then(frame0.endStream).isFalse();
        then(frame0.streamId).isEqualTo(STREAM_ID);
        then(frame0.data.readableBytes()).isEqualTo(data.length);

        channel.finishAndReleaseAll();
    }

    @Test
    void testWriteUnsupportedData() throws Exception {
        final HandleRegistry registry = new HandleRegistry(2, 1);
        setUp(registry);

        final Http2ConnectionHandler handler = channel.pipeline().get(Http2ConnectionHandler.class);

        ChannelPromise promise = channel.newPromise();
        handler.writeData(STREAM_ID, new Object(),
                false, promise);
        channel.flush();
        channel.readOutbound();
        assertFalse(promise.isSuccess());
        then(promise.cause()).isInstanceOf(IllegalArgumentException.class);
        Object msg = channel.readOutbound();
        then(msg).isNull();

        channel.finishAndReleaseAll();
    }

    @Test
    void testGetRegistry() throws Exception {
        final HandleRegistry registry = new HandleRegistry(2, 1);
        setUp(registry);

        final Http2ConnectionHandler handler = channel.pipeline().get(Http2ConnectionHandler.class);
        then(handler.getRegistry()).isSameAs(registry);
    }

    @Test
    void testWriteGoAwayOnExhaustion() throws Exception {
        final HandleRegistry registry = new HandleRegistry(2, 1);
        setUp(registry);

        final Http2ConnectionHandler handler = channel.pipeline().get(Http2ConnectionHandler.class);
        handler.writeGoAwayOnExhaustion(channel.newPromise());
        channel.flush();
        channel.readOutbound();

        Helper.GoawayFrame frame = channel.readOutbound();
        then(frame.lastStreamId).isEqualTo(Integer.MAX_VALUE - 1);
        then(frame.errorCode).isEqualTo(Http2Error.NO_ERROR.code());

        channel.finishAndReleaseAll();
    }
}
