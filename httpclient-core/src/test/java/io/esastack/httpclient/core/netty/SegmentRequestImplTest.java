
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
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.netty.buffer.BufferImpl;
import io.esastack.httpclient.core.Handler;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.SegmentRequest;
import io.esastack.httpclient.core.exec.ExecContext;
import io.esastack.httpclient.core.exec.RequestExecutor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SegmentRequestImplTest {

    private static final String COMMON_DATA = "Hello World";
    private static final String END = "It's end";

    @Test
    void testWriteArrayDataBeforeSegmentWriterCompleted() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final CompletableFuture<SegmentWriter> writerPromise = new CompletableFuture<>();

        final RequestExecutor executor = mock(RequestExecutor.class);
        final EmbeddedChannel channel = new EmbeddedChannel();
        when(executor.execute(any(HttpRequest.class),
                any(ExecContext.class)))
                .thenAnswer(answer -> {
                    final NettyExecContext ctx = answer.getArgument(1);
                    ctx.segmentWriter(writerPromise);
                    return response;
                });

        final SegmentRequest request = new SegmentRequestImpl(HttpClient.create(),
                executor, HttpMethod.POST, "http://127.0.0.1/chunked");

        final byte[] dataToWrite = new byte[1024 * 1025 + 512];
        ThreadLocalRandom.current().nextBytes(dataToWrite);

        for (int i = 0; i < 1025; i++) {
            byte[] temp = new byte[1024];
            System.arraycopy(dataToWrite, i * 1024, temp, 0, temp.length);
            request.write(temp);
        }
        request.write(dataToWrite, 1024 * 1025, 512);
        request.end();

        final ByteBuf out = Unpooled.buffer();
        final SegmentWriter writer = mock(SegmentWriter.class);

        when(writer.channel()).thenReturn(channel);
        when(writer.write(any(), anyInt(), anyInt()))
                .thenAnswer(answer -> {
                    final Object obj = answer.getArgument(0);
                    if (obj instanceof byte[]) {
                        final byte[] data = (byte[]) obj;
                        final int offset = answer.getArgument(1);
                        final int length = answer.getArgument(2);
                        out.writeBytes(data, offset, length);
                    } // Ignore empty end content.

                    return channel.newSucceededFuture();
                });
        when(writer.end()).thenAnswer(answer -> channel.newSucceededFuture());

        writerPromise.complete(writer);
        then(out.readableBytes()).isEqualTo(1024 * 1025 + 512);

        final byte[] outArray = new byte[1024 * 1025 + 512];
        out.readBytes(outArray);
        then(Arrays.equals(dataToWrite, outArray)).isTrue();

        then(response.isDone()).isFalse();

        // Test copy
        final SegmentRequest copied = request.copy();
        then(copied.headers().size()).isEqualTo(request.headers().size());
        then(copied.paramNames().size()).isEqualTo(request.paramNames().size());
        then(copied.isFile()).isEqualTo(request.isFile());
        then(copied.isMultipart()).isEqualTo(request.isMultipart());
        then(copied.isSegmented()).isEqualTo(request.isSegmented());
    }

    @Test
    void testWriteBufferDataBeforeSegmentWriterCompleted() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final CompletableFuture<SegmentWriter> writerPromise = new CompletableFuture<>();

        final RequestExecutor executor = mock(RequestExecutor.class);
        final EmbeddedChannel channel = new EmbeddedChannel();
        when(executor.execute(any(HttpRequest.class),
                any(ExecContext.class)))
                .thenAnswer(answer -> {
                    final NettyExecContext ctx = answer.getArgument(1);
                    ctx.segmentWriter(writerPromise);
                    return response;
                });

        final SegmentRequest request = new SegmentRequestImpl(HttpClient.create(),
                executor, HttpMethod.POST, "http://127.0.0.1/chunked");

        for (int i = 0; i < 10; i++) {
            request.write(new BufferImpl(Unpooled.buffer().writeBytes((COMMON_DATA + i).getBytes())));
        }

        request.end(new BufferImpl(Unpooled.buffer().writeBytes(END.getBytes())));
        final List<byte[]> data = new LinkedList<>();
        final SegmentWriter writer = mock(SegmentWriter.class);

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
    void testNoMatterWhenWriteSegmentedFailure() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final CompletableFuture<SegmentWriter> writerPromise = new CompletableFuture<>();

        final RequestExecutor executor = mock(RequestExecutor.class);
        final EmbeddedChannel channel = new EmbeddedChannel();
        when(executor.execute(any(HttpRequest.class),
                any(ExecContext.class)))
                .thenAnswer(answer -> {
                    final NettyExecContext ctx = answer.getArgument(1);
                    ctx.segmentWriter(writerPromise);
                    return response;
                });

        final SegmentRequest request = new SegmentRequestImpl(HttpClient.create(),
                executor, HttpMethod.POST, "http://127.0.0.1/chunked");

        for (int i = 0; i < 10; i++) {
            request.write((COMMON_DATA + i).getBytes());
        }

        request.end(END.getBytes());
        final List<byte[]> data = new LinkedList<>();
        final SegmentWriter writer = mock(SegmentWriter.class);

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
    void testWriteArrayDataAfterSegmentWriterCompleted() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final CompletableFuture<SegmentWriter> writerPromise = new CompletableFuture<>();

        final RequestExecutor executor = mock(RequestExecutor.class);
        final EmbeddedChannel channel = new EmbeddedChannel();
        when(executor.execute(any(HttpRequest.class),
                any(ExecContext.class)))
                .thenAnswer(answer -> {
                    final NettyExecContext ctx = answer.getArgument(1);
                    ctx.segmentWriter(writerPromise);
                    return response;
                });

        final SegmentRequest request = new SegmentRequestImpl(HttpClient.create(),
                executor, HttpMethod.POST, "http://127.0.0.1/chunked");

        final List<byte[]> data = new LinkedList<>();
        final SegmentWriter writer = mock(SegmentWriter.class);

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
        final CompletableFuture<SegmentWriter> writerPromise = new CompletableFuture<>();

        final RequestExecutor executor = mock(RequestExecutor.class);
        final EmbeddedChannel channel = new EmbeddedChannel();
        when(executor.execute(any(HttpRequest.class),
                any(ExecContext.class)))
                .thenAnswer(answer -> {
                    final NettyExecContext ctx = answer.getArgument(1);
                    ctx.segmentWriter(writerPromise);
                    return response;
                });

        final SegmentRequest request = new SegmentRequestImpl(HttpClient.create(),
                executor, HttpMethod.POST, "http://127.0.0.1/chunked");

        final List<byte[]> data = new LinkedList<>();
        final SegmentWriter writer = mock(SegmentWriter.class);

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
        final CompletableFuture<SegmentWriter> writerPromise = new CompletableFuture<>();

        final RequestExecutor executor = mock(RequestExecutor.class);
        when(executor.execute(any(HttpRequest.class),
                any(ExecContext.class)))
                .thenAnswer(answer -> {
                    final NettyExecContext ctx = answer.getArgument(1);
                    ctx.segmentWriter(writerPromise);
                    return response;
                });

        final SegmentRequest request = new SegmentRequestImpl(HttpClient.create(),
                executor, HttpMethod.POST, "http://127.0.0.1/chunked");

        request.write(COMMON_DATA.getBytes());

        final Channel channel = mock(Channel.class);
        final SegmentWriter writer = mock(SegmentWriter.class);
        writerPromise.complete(writer);
        when(writer.channel()).thenReturn(channel);
        when(channel.isWritable()).thenReturn(false);
        then(request.isWritable()).isFalse();
        when(channel.isWritable()).thenReturn(true);
        then(request.isWritable()).isTrue();
    }

    @Test
    void testUnmodifiableAfterStarted() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final CompletableFuture<SegmentWriter> writerPromise = new CompletableFuture<>();
        writerPromise.complete(mock(SegmentWriter.class));

        final RequestExecutor executor = mock(RequestExecutor.class);
        when(executor.execute(any(HttpRequest.class),
                any(ExecContext.class)))
                .thenAnswer(answer -> {
                    final NettyExecContext ctx = answer.getArgument(1);
                    ctx.segmentWriter(writerPromise);
                    return response;
                });

        final SegmentRequest request = new SegmentRequestImpl(HttpClient.create(),
                executor, HttpMethod.POST, "http://127.0.0.1/chunked");

        final byte[] dataToWrite = new byte[1024 * 1025 + 512];
        ThreadLocalRandom.current().nextBytes(dataToWrite);

        // Before writing
        request.disableExpectContinue();

        request.enableUriEncode();
        then(request.uriEncode()).isTrue();

        request.maxRedirects(10);
        request.maxRetries(10);

        request.readTimeout(100);
        then(request.readTimeout()).isEqualTo(100);

        request.addHeaders(Collections.singletonMap("a", "b"));
        then(request.getHeader("a")).isEqualTo("b");

        request.addParams(Collections.singletonMap("m", "n"));
        then(request.getParam("m")).isEqualTo("n");

        request.handle((h) -> {
        });
        request.handler(mock(Handler.class));

        request.addHeader("x", "y");
        then(request.getHeader("x")).isEqualTo("y");

        request.setHeader("a", "bb");
        then(request.getHeader("a")).isEqualTo("bb");

        request.removeHeader("a");
        then(request.getHeader("a")).isNullOrEmpty();

        request.addParam("p", "q");
        then(request.getParam("p")).isEqualTo("q");

        request.write(dataToWrite);

        // After writing
        assertThrows(IllegalStateException.class, request::disableExpectContinue);
        assertThrows(IllegalStateException.class, request::enableUriEncode);
        assertThrows(IllegalStateException.class, () -> request.maxRedirects(10));
        assertThrows(IllegalStateException.class, () -> request.maxRetries(10));
        assertThrows(IllegalStateException.class, () -> request.readTimeout(100));
        assertThrows(IllegalStateException.class, () -> request.addParams(Collections.singletonMap("m", "n")));
        assertThrows(IllegalStateException.class, () -> request.handle((h) -> {
        }));
        assertThrows(IllegalStateException.class, () -> request.handler(mock(Handler.class)));

        assertThrows(IllegalStateException.class, () -> request.addParam("p", "q"));

        // Header ops are allowed
        request.addHeaders(Collections.singletonMap("a", "b"));
        request.addHeader("x", "y");
        request.setHeader("a", "bb");
        request.removeHeader("a");
    }

}
