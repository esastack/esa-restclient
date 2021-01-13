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
import esa.httpclient.core.ChunkRequest;
import esa.httpclient.core.Handle;
import esa.httpclient.core.Handler;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.HttpRequestBaseImpl;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.ListenerProxy;
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
 * The implementation of {@link ChunkRequest} based on netty.
 */
public class ChunkRequestImpl extends HttpRequestBaseImpl implements ChunkRequest {

    private static final IllegalStateException REQUEST_HAS_ENDED = new IllegalStateException("Request has ended");
    private static final IllegalStateException CHANNEL_IS_NULL = new IllegalStateException("Channel is null");

    private static final byte[] EMPTY_BYTES = new byte[0];

    private final RequestExecutor executor;

    private volatile CompletableFuture<HttpResponse> response;
    private volatile CompletableFuture<ChunkWriter> chunkWriter;

    private CompletableFuture<ChunkWriter> orderedWriterOpsChain;

    private boolean started;
    private boolean ended;

    ChunkRequestImpl(HttpClientBuilder builder,
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
        if (chunkWriter == null) {
            return false;
        }

        if (!chunkWriter.isDone()) {
            return false;
        }

        Channel channel;
        if ((channel = chunkWriter.getNow(null).channel()) != null) {
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
            if (chunkWriter.isDone() && chunkWriter.isCompletedExceptionally()) {
                return Futures.completed(new IOException("Failed to acquire connection",
                        Futures.getCause(chunkWriter)));
            } else {
                final CompletableFuture<Void> result = new CompletableFuture<>();
                final BiConsumer<ChunkWriter, Throwable> consumer = (wr, th) ->
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
            return Futures.completed(new IOException("Unexpected error while writing content", th));
        }
    }

    private <T> CompletableFuture<Void> doWrite(ChunkWriter writer,
                                                T data,
                                                int offset,
                                                int length) {
        Channel channel;
        if (writer == null || (channel = writer.channel()) == null) {
            return Futures.completed(CHANNEL_IS_NULL);
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
            if (chunkWriter.isDone() && chunkWriter.isCompletedExceptionally()) {
                return Futures.completed(new IOException("Failed to acquire connection",
                        Futures.getCause(chunkWriter)));
            } else {
                final CompletableFuture<Void> result = new CompletableFuture<>();
                final BiConsumer<ChunkWriter, Throwable> consumer = (wr, th) ->
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
            return Futures.completed(new IOException("Unexpected error while ending request", th));
        }
    }

    private CompletableFuture<Void> doEnd(ChunkWriter writer, HttpHeaders headers) {
        if (writer == null || writer.channel() == null) {
            return Futures.completed(CHANNEL_IS_NULL);
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
        if (chunkWriter != null) {
            return;
        }

        synchronized (this) {
            if (chunkWriter != null) {
                return;
            }

            this.started = true;
            response = executor.execute(this, ctx, ListenerProxy.DEFAULT, handle, handler);
            chunkWriter = ctx.getWriter().orElse(null);
        }
    }

    private BiConsumer<Void, Throwable> handleOnEnd(Consumer<Throwable> handle) {
        return (v, th) -> {
            if (handle != null) {
                handle.accept(th);
            }

            ChunkWriter writer;
            if ((writer = chunkWriter.getNow(null)) != null) {
                writer.close(th);
            }
            if (th != null) {
                response.completeExceptionally(th);
            }
        };
    }

    private synchronized void appendToOrderedWriterOpsChain(BiConsumer<ChunkWriter, Throwable> consumer) {
        if (orderedWriterOpsChain == null) {
            orderedWriterOpsChain = chunkWriter.whenComplete(consumer);
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
            throw new IllegalStateException("Request has started to execute" +
                    " and the modification isn't allowed");
        }
    }

    @Override
    public ChunkRequest copy() {
        final ChunkRequestImpl copied = new ChunkRequestImpl(builder, executor, method(), uri().toString());
        copyTo(this, copied);
        return copied;
    }

    @Override
    public synchronized ChunkRequest uriEncodeEnabled(Boolean uriEncodeEnabled) {
        checkStarted();
        super.uriEncodeEnabled(uriEncodeEnabled);
        return this;
    }

    @Override
    public synchronized ChunkRequest expectContinueEnabled(Boolean expectContinueEnabled) {
        checkStarted();
        super.expectContinueEnabled(expectContinueEnabled);
        return this;
    }

    @Override
    public synchronized ChunkRequest maxRedirects(int maxRedirects) {
        checkStarted();
        super.maxRedirects(maxRedirects);
        return this;
    }

    @Override
    public synchronized ChunkRequest maxRetries(int maxRetries) {
        checkStarted();
        super.maxRetries(maxRetries);
        return this;
    }

    @Override
    public synchronized ChunkRequest readTimeout(int readTimeout) {
        checkStarted();
        super.readTimeout(readTimeout);
        return this;
    }

    @Override
    public synchronized ChunkRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        checkStarted();
        super.addHeaders(headers);
        return this;
    }

    @Override
    public synchronized ChunkRequest addHeader(CharSequence name, CharSequence value) {
        checkStarted();
        super.addHeader(name, value);
        return this;
    }

    @Override
    public synchronized ChunkRequest setHeader(CharSequence name, CharSequence value) {
        checkStarted();
        super.setHeader(name, value);
        return this;
    }

    @Override
    public synchronized ChunkRequest removeHeader(CharSequence name) {
        checkStarted();
        super.removeHeader(name);
        return this;
    }

    @Override
    public synchronized ChunkRequest addParams(Map<String, String> params) {
        checkStarted();
        super.addParams(params);
        return this;
    }

    @Override
    public synchronized ChunkRequest addParam(String name, String value) {
        checkStarted();
        super.addParam(name, value);
        return this;
    }

    @Override
    public synchronized ChunkRequest handle(Consumer<Handle> handle) {
        checkStarted();
        super.handle(handle);
        return this;
    }

    @Override
    public synchronized ChunkRequest handler(Handler handler) {
        checkStarted();
        super.handler(handler);
        return this;
    }
}
