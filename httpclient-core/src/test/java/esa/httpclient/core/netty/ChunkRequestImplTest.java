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
import esa.commons.netty.core.BufferImpl;
import esa.httpclient.core.ChunkRequest;
import esa.httpclient.core.Context;
import esa.httpclient.core.ContextImpl;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.RequestOptions;
import esa.httpclient.core.exec.RequestExecutor;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChunkRequestImplTest {

    private static final String COMMON_DATA = "Hello World";
    private static final String END = "It's end";

    @Test
    void testWriteArrayDataBeforeChunkWriterCompleted() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final CompletableFuture<ChunkWriter> writerPromise = new CompletableFuture<>();

        final RequestExecutor executor = mock(RequestExecutor.class);
        final EmbeddedChannel channel = new EmbeddedChannel();
        when(executor.async(any(HttpRequest.class),
                any(Context.class),
                any(Listener.class)))
                .thenAnswer(answer -> {
                    final Context ctx = answer.getArgument(1);
                    ctx.setAttr(NettyTransceiver.CHUNK_WRITER, writerPromise);
                    return response;
                });

        final ChunkRequest request = new ChunkRequestImpl(executor, mock(RequestOptions.class),
                new ContextImpl(), true);

        for (int i = 0; i < 10; i++) {
            request.write((COMMON_DATA + i).getBytes());
        }

        request.end(END.getBytes());
        final List<byte[]> data = new LinkedList<>();
        final ChunkWriter writer = mock(ChunkWriter.class);

        when(writer.channel()).thenReturn(channel);
        when(writer.write(any(), anyInt(), anyInt()))
                .thenAnswer(answer -> {
                    if (answer.getArgument(0) instanceof byte[]) {
                        data.add(answer.getArgument(0));
                    }
                    return channel.newSucceededFuture();
                });
        when(writer.end()).thenAnswer(answer -> channel.newSucceededFuture());

        writerPromise.complete(writer);
        then(data.size()).isEqualTo(11);

        for (int i = 0; i < data.size() - 1; i++) {
            then(new String(data.get(i))).isEqualTo((COMMON_DATA + i));
        }
        then(new String(data.get(10))).isEqualTo(END);
        then(response.isDone()).isFalse();
    }

    @Test
    void testWriteBufferDataBeforeChunkWriterCompleted() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final CompletableFuture<ChunkWriter> writerPromise = new CompletableFuture<>();

        final RequestExecutor executor = mock(RequestExecutor.class);
        final EmbeddedChannel channel = new EmbeddedChannel();
        when(executor.async(any(HttpRequest.class),
                any(Context.class),
                any(Listener.class)))
                .thenAnswer(answer -> {
                    final Context ctx = answer.getArgument(1);
                    ctx.setAttr(NettyTransceiver.CHUNK_WRITER, writerPromise);
                    return response;
                });

        final ChunkRequest request = new ChunkRequestImpl(executor, mock(RequestOptions.class),
                new ContextImpl(), true);

        for (int i = 0; i < 10; i++) {
            request.write(new BufferImpl(Unpooled.buffer().writeBytes((COMMON_DATA + i).getBytes())));
        }

        request.end(new BufferImpl(Unpooled.buffer().writeBytes(END.getBytes())));
        final List<byte[]> data = new LinkedList<>();
        final ChunkWriter writer = mock(ChunkWriter.class);

        when(writer.channel()).thenReturn(channel);
        when(writer.write(any(), anyInt(), anyInt()))
                .thenAnswer(answer -> {
                    if (answer.getArgument(0) instanceof Buffer) {
                        byte[] temp = new byte[((Buffer) answer.getArgument(0)).readableBytes()];
                        ((Buffer) answer.getArgument(0)).readBytes(temp);
                        data.add(temp);
                    }
                    return channel.newSucceededFuture();
                });
        when(writer.end()).thenAnswer(answer -> channel.newSucceededFuture());

        writerPromise.complete(writer);
        then(data.size()).isEqualTo(11);

        for (int i = 0; i < data.size() - 1; i++) {
            then(new String(data.get(i))).isEqualTo((COMMON_DATA + i));
        }
        then(new String(data.get(10))).isEqualTo(END);
        then(response.isDone()).isFalse();
    }

    @Test
    void testNoMatterWhenWriteChunkedFailure() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final CompletableFuture<ChunkWriter> writerPromise = new CompletableFuture<>();

        final RequestExecutor executor = mock(RequestExecutor.class);
        final EmbeddedChannel channel = new EmbeddedChannel();
        when(executor.async(any(HttpRequest.class),
                any(Context.class),
                any(Listener.class)))
                .thenAnswer(answer -> {
                    final Context ctx = answer.getArgument(1);
                    ctx.setAttr(NettyTransceiver.CHUNK_WRITER, writerPromise);
                    return response;
                });

        final ChunkRequest request = new ChunkRequestImpl(executor, mock(RequestOptions.class),
                new ContextImpl(), true);

        for (int i = 0; i < 10; i++) {
            request.write((COMMON_DATA + i).getBytes());
        }

        request.end(END.getBytes());
        final List<byte[]> data = new LinkedList<>();
        final ChunkWriter writer = mock(ChunkWriter.class);

        when(writer.channel()).thenReturn(channel);
        when(writer.write(any(), anyInt(), anyInt()))
                .thenAnswer(answer -> {
                    if (answer.getArgument(0) instanceof byte[]) {
                        data.add(answer.getArgument(0));
                    }
                    return channel.newFailedFuture(new IOException());
                });
        when(writer.end()).thenAnswer(answer -> channel.newSucceededFuture());

        writerPromise.complete(writer);
        then(data.size()).isEqualTo(11);

        for (int i = 0; i < data.size() - 1; i++) {
            then(new String(data.get(i))).isEqualTo((COMMON_DATA + i));
        }
        then(new String(data.get(10))).isEqualTo(END);
        then(response.isDone()).isFalse();
    }

    @Test
    void testWriteArrayDataAfterChunkWriterCompleted() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final CompletableFuture<ChunkWriter> writerPromise = new CompletableFuture<>();

        final RequestExecutor executor = mock(RequestExecutor.class);
        final EmbeddedChannel channel = new EmbeddedChannel();
        when(executor.async(any(HttpRequest.class),
                any(Context.class),
                any(Listener.class)))
                .thenAnswer(answer -> {
                    final Context ctx = answer.getArgument(1);
                    ctx.setAttr(NettyTransceiver.CHUNK_WRITER, writerPromise);
                    return response;
                });

        final ChunkRequest request = new ChunkRequestImpl(executor, mock(RequestOptions.class),
                new ContextImpl(), true);

        final List<byte[]> data = new LinkedList<>();
        final ChunkWriter writer = mock(ChunkWriter.class);

        when(writer.channel()).thenReturn(channel);
        when(writer.write(any(), anyInt(), anyInt()))
                .thenAnswer(answer -> {
                    if (answer.getArgument(0) instanceof byte[]) {
                        data.add(answer.getArgument(0));
                    }
                    return channel.newSucceededFuture();
                });
        when(writer.end()).thenAnswer(answer -> channel.newSucceededFuture());

        writerPromise.complete(writer);

        for (int i = 0; i < 10; i++) {
            request.write((COMMON_DATA + i).getBytes());
        }

        final AtomicBoolean ended = new AtomicBoolean();
        final Consumer<Throwable> endHandle = throwable -> ended.set(true);
        request.end(END.getBytes(), endHandle);

        then(data.size()).isEqualTo(11);

        for (int i = 0; i < data.size() - 1; i++) {
            then(new String(data.get(i))).isEqualTo((COMMON_DATA + i));
        }
        then(new String(data.get(10))).isEqualTo(END);
        then(response.isDone()).isFalse();
        then(ended.get()).isTrue();
    }

    @Test
    void testErrorWhileEnding() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final CompletableFuture<ChunkWriter> writerPromise = new CompletableFuture<>();

        final RequestExecutor executor = mock(RequestExecutor.class);
        final EmbeddedChannel channel = new EmbeddedChannel();
        when(executor.async(any(HttpRequest.class),
                any(Context.class),
                any(Listener.class)))
                .thenAnswer(answer -> {
                    final Context ctx = answer.getArgument(1);
                    ctx.setAttr(NettyTransceiver.CHUNK_WRITER, writerPromise);
                    return response;
                });

        final ChunkRequest request = new ChunkRequestImpl(executor, mock(RequestOptions.class),
                new ContextImpl(), true);

        final List<byte[]> data = new LinkedList<>();
        final ChunkWriter writer = mock(ChunkWriter.class);

        when(writer.channel()).thenReturn(channel);
        when(writer.write(any(), anyInt(), anyInt()))
                .thenAnswer(answer -> {
                    if (answer.getArgument(0) instanceof byte[]) {
                        data.add(answer.getArgument(0));
                    }
                    return channel.newSucceededFuture();
                });
        when(writer.end()).thenAnswer(answer -> channel.newSucceededFuture());

        writerPromise.complete(writer);

        for (int i = 0; i < 10; i++) {
            request.write((COMMON_DATA + i).getBytes());
        }

        request.write(END.getBytes());

        when(writer.channel()).thenReturn(null);

        final AtomicBoolean ended = new AtomicBoolean();
        final Consumer<Throwable> endHandle = throwable -> ended.set(true);
        request.end(endHandle);

        then(data.size()).isEqualTo(11);

        for (int i = 0; i < data.size() - 1; i++) {
            then(new String(data.get(i))).isEqualTo((COMMON_DATA + i));
        }

        then(new String(data.get(10))).isEqualTo(END);
        then(response.isDone()).isTrue();
        then(response.isCompletedExceptionally()).isTrue();
        then(ended.get()).isTrue();
    }

    @Test
    void testIsWritable() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final CompletableFuture<ChunkWriter> writerPromise = new CompletableFuture<>();

        final RequestExecutor executor = mock(RequestExecutor.class);
        when(executor.async(any(HttpRequest.class),
                any(Context.class),
                any(Listener.class)))
                .thenAnswer(answer -> {
                    final Context ctx = answer.getArgument(1);
                    ctx.setAttr(NettyTransceiver.CHUNK_WRITER, writerPromise);
                    return response;
                });

        final ChunkRequest request = new ChunkRequestImpl(executor, mock(RequestOptions.class),
                new ContextImpl(), true);
        request.write(COMMON_DATA.getBytes());

        final Channel channel = mock(Channel.class);
        final ChunkWriter writer = mock(ChunkWriter.class);
        writerPromise.complete(writer);
        when(writer.channel()).thenReturn(channel);
        when(channel.isWritable()).thenReturn(false);
        then(request.isWritable()).isFalse();
        then(request.aggregate()).isTrue();
        when(channel.isWritable()).thenReturn(true);
        then(request.isWritable()).isTrue();
    }

}
