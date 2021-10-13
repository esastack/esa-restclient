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

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.Handle;
import esa.httpclient.core.Handler;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.HttpRequestBaseImpl;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.ListenerProxy;
import esa.httpclient.core.SegmentRequest;
import esa.httpclient.core.exec.RequestExecutor;
import esa.httpclient.core.util.Futures;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.internal.MathUtil;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The implementation of {@link SegmentRequest} based on netty.
 */
public class SegmentRequestImpl extends HttpRequestBaseImpl implements SegmentRequest {

    private static final IllegalStateException REQUEST_HAS_ENDED = new IllegalStateException("Request has ended");
    private static final IllegalStateException CONNECTION_IS_NULL = new IllegalStateException("Connection is null");

    private static final byte[] EMPTY_BYTES = new byte[0];

    private final RequestExecutor executor;

    private volatile CompletableFuture<HttpResponse> response;
    private volatile CompletableFuture<SegmentWriter> segmentWriter;

    private CompletableFuture<SegmentWriter> orderedWriterOpsChain;

    private boolean started;
    private boolean ended;

    SegmentRequestImpl(HttpClientBuilder builder,
                       RequestExecutor executor,
                       HttpMethod method,
                       String uri) {
        super(builder, method, uri);
        Checks.checkNotNull(executor, "RequestExecutor must not be null");
        this.executor = executor;
    }

    @Override
    public CompletableFuture<Void> write(byte[] data, int offset, int length) {
        if (data == null) {
            data = EMPTY_BYTES;
        }
        checkIndex(data, offset, length);

        checkAndStart();

        return checkAndWrite(data, offset, length);
    }

    @Override
    public CompletableFuture<Void> write(Buffer data) {
        checkAndStart();

        return checkAndWrite(data, -1, -1);
    }

    @Override
    public CompletableFuture<HttpResponse> end(byte[] data, int offset, int length, Consumer<Throwable> handle) {
        if (data == null) {
            data = EMPTY_BYTES;
        }
        checkIndex(data, offset, length);

        // Check and write data and then end the request, if any exception caught,
        // we should close the request.
        checkAndWrite(data, offset, length);
        safelyDoEnd(null).whenComplete(handleOnEnd(handle));

        return response;
    }

    @Override
    public CompletableFuture<HttpResponse> end(Consumer<Throwable> handle) {
        checkAndStart();

        safelyDoEnd(null).whenComplete(handleOnEnd(handle));

        return response;
    }

    @Override
    public CompletableFuture<HttpResponse> end(HttpHeaders trailers, Consumer<Throwable> handle) {
        checkAndStart();

        safelyDoEnd(trailers).whenComplete(handleOnEnd(handle));
        return response;
    }

    @Override
    public CompletableFuture<HttpResponse> end(Buffer data, Consumer<Throwable> handle) {
        checkAndStart();

        // Check and write data and then end the request, if any exception caught,
        // we should close the request.
        checkAndWrite(data, -1, -1);
        safelyDoEnd(null).whenComplete(handleOnEnd(handle));

        return response;
    }

    @Override
    public boolean isWritable() {
        if (segmentWriter == null) {
            return false;
        }

        if (!segmentWriter.isDone()) {
            return false;
        }

        Channel channel;
        if ((channel = segmentWriter.getNow(null).channel()) != null) {
            return channel.isWritable();
        }
        return false;
    }

    private static void checkIndex(byte[] bytes, int off, int len) {
        if (MathUtil.isOutOfBounds(off, len, bytes.length)) {
            throw new IndexOutOfBoundsException();
        }
    }

    private <T> CompletableFuture<Void> checkAndWrite(T data, int offset, int length) {
        try {
            if (segmentWriter.isDone() && segmentWriter.isCompletedExceptionally()) {
                return Futures.completed(new IOException("Failed to acquire connection",
                        Futures.getCause(segmentWriter)));
            } else {
                final CompletableFuture<Void> result = new CompletableFuture<>();
                final BiConsumer<SegmentWriter, Throwable> consumer = (wr, th) ->
                        doWrite(wr, data, offset, length).whenComplete((v, th0) -> {
                            if (th0 != null) {
                                result.completeExceptionally(th0);
                            } else {
                                result.complete(null);
                            }
                        });

                appendToOrderedWriterOpsChain(consumer);

                return result;
            }
        } catch (Throwable th) {
            return Futures.completed(new IOException("Unexpected error occurred when writing content", th));
        }
    }

    private <T> CompletableFuture<Void> doWrite(SegmentWriter writer,
                                                T data,
                                                int offset,
                                                int length) {
        Channel channel;
        if (writer == null || (channel = writer.channel()) == null) {
            return Futures.completed(CONNECTION_IS_NULL);
        }

        final CompletableFuture<Void> future = new CompletableFuture<>();

        // Note: Do write in a fixed single thread, so we need't to consider concurrency conflicts.
        if (channel.eventLoop().inEventLoop()) {
            if (ended) {
                future.completeExceptionally(REQUEST_HAS_ENDED);
            } else {
                joinToComplete(future, writer.write(data, offset, length));
            }
        } else {
            channel.eventLoop().execute(() -> {
                if (ended) {
                    future.completeExceptionally(REQUEST_HAS_ENDED);
                } else {
                    joinToComplete(future, writer.write(data, offset, length));
                }
            });
        }

        if (data instanceof Buffer) {
            return future.whenComplete((rsp, th) -> Utils.tryRelease(((Buffer) data).getByteBuf()));
        } else {
            return future;
        }
    }

    private CompletableFuture<Void> safelyDoEnd(HttpHeaders headers) {
        try {
            if (segmentWriter.isDone() && segmentWriter.isCompletedExceptionally()) {
                return Futures.completed(new IOException("Failed to acquire connection",
                        Futures.getCause(segmentWriter)));
            } else {
                final CompletableFuture<Void> result = new CompletableFuture<>();
                final BiConsumer<SegmentWriter, Throwable> consumer = (wr, th) ->
                        doEnd(wr, headers).whenComplete((v, th0) -> {
                            if (th0 != null) {
                                result.completeExceptionally(th0);
                            } else {
                                result.complete(null);
                            }
                        });

                appendToOrderedWriterOpsChain(consumer);

                return result;
            }
        } catch (Throwable th) {
            return Futures.completed(new IOException("Unexpected error occurred when ending request", th));
        }
    }

    private CompletableFuture<Void> doEnd(SegmentWriter writer, HttpHeaders headers) {
        if (writer == null || writer.channel() == null) {
            return Futures.completed(CONNECTION_IS_NULL);
        }

        final Channel channel = writer.channel();
        final CompletableFuture<Void> promise = new CompletableFuture<>();

        final Runnable endTask = () -> {
            if (ended) {
                promise.completeExceptionally(REQUEST_HAS_ENDED);
            }
            ended = true;
            final ChannelFuture endPromise;
            if (headers == null) {
                endPromise = writer.end();
            } else {
                endPromise = writer.end(headers);
            }
            joinToComplete(promise, endPromise);
        };

        Utils.runInChannel(channel, endTask);

        return promise;
    }

    private void checkAndStart() {
        if (segmentWriter != null) {
            return;
        }

        synchronized (this) {
            if (segmentWriter != null) {
                return;
            }

            this.started = true;
            response = executor.execute(this, ctx, ListenerProxy.DEFAULT, handle, handler);
            segmentWriter = ctx.getWriter().orElse(null);
        }
    }

    private BiConsumer<Void, Throwable> handleOnEnd(Consumer<Throwable> handle) {
        return (v, th) -> {
            if (handle != null) {
                handle.accept(th);
            }

            SegmentWriter writer;
            if ((writer = segmentWriter.getNow(null)) != null) {
                writer.close(th);
            }
            if (th != null) {
                response.completeExceptionally(th);
            }
        };
    }

    private synchronized void appendToOrderedWriterOpsChain(BiConsumer<SegmentWriter, Throwable> consumer) {
        if (orderedWriterOpsChain == null) {
            orderedWriterOpsChain = segmentWriter.whenComplete(consumer);
        } else {
            orderedWriterOpsChain = orderedWriterOpsChain.whenComplete(consumer);
        }
    }

    private static void joinToComplete(CompletableFuture<Void> promise, ChannelFuture future) {
        if (future.isDone()) {
            if (future.isSuccess()) {
                promise.complete(null);
            } else {
                promise.completeExceptionally(future.cause());
            }
        } else {
            future.addListener(f -> {
                if (f.isSuccess()) {
                    promise.complete(null);
                } else {
                    promise.completeExceptionally(f.cause());
                }
            });
        }
    }

    private void checkStarted() {
        if (started) {
            throw new IllegalStateException("Request's execute() has been called " +
                    " and the modification isn't allowed");
        }
    }

    @Override
    public SegmentRequest copy() {
        final SegmentRequestImpl copied = new SegmentRequestImpl(builder, executor, method(), uri().toString());
        copyTo(this, copied);
        return copied;
    }

    @Override
    public SegmentRequest enableUriEncode() {
        checkStarted();
        super.enableUriEncode();
        return this;
    }

    @Override
    public SegmentRequest disableExpectContinue() {
        checkStarted();
        super.disableExpectContinue();
        return this;
    }

    @Override
    public SegmentRequest maxRedirects(int maxRedirects) {
        checkStarted();
        super.maxRedirects(maxRedirects);
        return this;
    }

    @Override
    public SegmentRequest maxRetries(int maxRetries) {
        checkStarted();
        super.maxRetries(maxRetries);
        return this;
    }

    @Override
    public SegmentRequest readTimeout(long readTimeout) {
        checkStarted();
        super.readTimeout(readTimeout);
        return this;
    }

    @Override
    public SegmentRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        super.addHeaders(headers);
        return this;
    }

    @Override
    public SegmentRequest addHeader(CharSequence name, CharSequence value) {
        super.addHeader(name, value);
        return this;
    }

    @Override
    public SegmentRequest setHeader(CharSequence name, CharSequence value) {
        super.setHeader(name, value);
        return this;
    }

    @Override
    public SegmentRequest removeHeader(CharSequence name) {
        super.removeHeader(name);
        return this;
    }

    @Override
    public SegmentRequest addParams(Map<String, String> params) {
        checkStarted();
        super.addParams(params);
        return this;
    }

    @Override
    public SegmentRequest addParam(String name, String value) {
        checkStarted();
        super.addParam(name, value);
        return this;
    }

    @Override
    public SegmentRequest handle(Consumer<Handle> handle) {
        checkStarted();
        super.handle(handle);
        return this;
    }

    @Override
    public SegmentRequest handler(Handler handler) {
        checkStarted();
        super.handler(handler);
        return this;
    }
}
