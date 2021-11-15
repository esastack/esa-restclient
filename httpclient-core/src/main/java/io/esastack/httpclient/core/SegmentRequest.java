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
package io.esastack.httpclient.core;

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.netty.buffer.BufferImpl;
import io.netty.buffer.Unpooled;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * This class is designed for writing data chunk by chunk. We recommend you to know that,
 * once you have began the request by any of {@link #write(Buffer)}s, and then a channel
 * or a stream will be allocated, so <strong>you must end the request manually by
 * any of {@link #end(Buffer)}s</strong>. If any exception was caught during {@link #end(Buffer)},
 * we will close the request automatically for releasing resources. Besides, you'd
 * better use {@link #isWritable()} to judge whether current request can continue writing
 * or not.
 */
public interface SegmentRequest extends HttpRequestBase {

    /**
     * Writes the chunked data to channel.
     *
     * <strong>Please make sure the {@code data} won't be modified until
     * the {@code future} has completed, otherwise maybe the modified data will
     * be wrote to channel</strong>.
     *
     * @param data  chunked data
     * @return result, which may be null or exception caught through writing.
     */
    default CompletableFuture<Void> write(byte[] data) {
        return write(data, 0);
    }

    /**
     * Writes the chunk data to channel.
     *
     * <strong>Please make sure the {@code data} won't be modified until
     * the {@code future} has completed, otherwise maybe the modified data will
     * be wrote to channel</strong>.
     *
     * @param data      data
     * @param offset    offset
     * @return result, which may be null or exception caught through writing.
     */
    default CompletableFuture<Void> write(byte[] data, int offset) {
        return write(data, offset, data == null ? 0 : data.length - offset);
    }

    /**
     * Writes the chunk data to channel.
     *
     * <strong>Please make sure the {@code data} won't be modified until
     * the {@code future} has completed, otherwise maybe the modified data will
     * be wrote to channel</strong>.
     *
     * @param data      data
     * @param offset    offset
     * @param length    length
     * @return result, which may be null or exception caught through writing.
     */
    CompletableFuture<Void> write(byte[] data, int offset, int length);

    /**
     * Writes the chunk data to channel.
     *
     * <strong>Please make sure the {@code data} won't be modified until
     * the {@code future} has completed, otherwise maybe the modified data will
     * be wrote to channel</strong>.
     *
     * @param data      data
     * @return result, which may be null or exception caught through writing.
     */
    CompletableFuture<Void> write(Buffer data);

    /**
     * Just end the current request and obtain the response asynchronously.
     *
     * @return      result, which may be response or exception caught through ending.
     */
    default CompletableFuture<HttpResponse> end() {
        return end(new BufferImpl(Unpooled.EMPTY_BUFFER), null);
    }

    /**
     * Just end the current request and obtain the response asynchronously.
     *
     * @param handle which can be used to handle the result of end, if succeed, throwable will be null.
     * @return      result, which may be response or exception caught through ending.
     */
    CompletableFuture<HttpResponse> end(Consumer<Throwable> handle);

    /**
     * Just end the current request with given {@code trailers} and obtains the response asynchronously.
     *
     * @param trailers      trailers
     * @return              result, which may be response or exception caught through ending.
     */
    default CompletableFuture<HttpResponse> end(HttpHeaders trailers) {
        return end(trailers, null);
    }

    /**
     * Just end the current request with given {@code trailers} and obtains the response asynchronously.
     *
     * @param trailers      trailers
     * @param handle which can be used to handle the result of end, if succeed, throwable will be null.
     * @return              result, which may be response or exception caught through ending.
     */
    CompletableFuture<HttpResponse> end(HttpHeaders trailers, Consumer<Throwable> handle);

    /**
     * Ends current request with given chunk data and obtains the response asynchronously.
     *
     * <strong>Please make sure the {@code data} won't be modified until
     * the {@code future} has completed, otherwise maybe the modified data will
     * be wrote to channel</strong>.
     *
     * @param data      chunked data
     * @return          result, which may be response or exception caught through ending.
     */
    default CompletableFuture<HttpResponse> end(byte[] data) {
        return end(data, 0);
    }

    /**
     * Ends current request with given chunk data and obtains the response asynchronously.
     *
     * <strong>Please make sure the {@code data} won't be modified until
     * the {@code future} has completed, otherwise maybe the modified data will
     * be wrote to channel</strong>.
     *
     * @param data      chunked data
     * @param handle which can be used to handle the result of end, if succeed, throwable will be null.
     * @return          result, which may be response or exception caught through ending.
     */
    default CompletableFuture<HttpResponse> end(byte[] data, Consumer<Throwable> handle) {
        return end(data, 0, handle);
    }

    /**
     * Ends current request with given chunk data and obtains the response asynchronously.
     *
     * <strong>Please make sure the {@code data} won't be modified until
     * the {@code future} has completed, otherwise maybe the modified data will
     * be wrote to channel</strong>.
     *
     * @param data      data
     * @param offset    offset
     * @return          result, which may be response or exception caught through ending.
     */
    default CompletableFuture<HttpResponse> end(byte[] data, int offset) {
        return end(data, offset, data == null ? 0 : data.length - offset, null);
    }

    /**
     * Ends current request with given chunk data and obtains the response asynchronously.
     *
     * <strong>Please make sure the {@code data} won't be modified until
     * the {@code future} has completed, otherwise maybe the modified data will
     * be wrote to channel</strong>.
     *
     * @param data      data
     * @param offset    offset
     * @param handle which can be used to handle the result of end, if succeed, throwable will be null.
     * @return          result, which may be response or exception caught through ending.
     */
    default CompletableFuture<HttpResponse> end(byte[] data, int offset, Consumer<Throwable> handle) {
        return end(data, offset, data == null ? 0 : data.length - offset, handle);
    }

    /**
     * Ends current request with given chunk data and obtain the response asynchronously.
     *
     * <strong>Please make sure the {@code data} won't be modified until
     * the {@code future} has completed, otherwise maybe the modified data will
     * be wrote to channel</strong>.
     *
     * @param data      data
     * @param offset    offset
     * @param length    length
     * @return          result, which may be response or exception caught through ending.
     */
    default CompletableFuture<HttpResponse> end(byte[] data, int offset, int length) {
        return end(data, offset, length, null);
    }

    /**
     * Ends current request with given chunk data and obtain the response asynchronously.
     *
     * <strong>Please make sure the {@code data} won't be modified until
     * the {@code future} has completed, otherwise maybe the modified data will
     * be wrote to channel</strong>.
     *
     * @param data data
     * @param offset    offset
     * @param length    length
     * @param handle which can be used to handle the result of end, if succeed, throwable will be null.
     * @return          result, which may be response or exception caught through ending.
     */
    CompletableFuture<HttpResponse> end(byte[] data, int offset, int length, Consumer<Throwable> handle);

    /**
     * Ends current request with given chunk data and obtains the response asynchronously.
     *
     * <strong>Please make sure the {@code data} won't be modified until
     * the {@code future} has completed, otherwise maybe the modified data will
     * be wrote to channel</strong>.
     *
     * @param data chunked data
     * @return response
     */
    default CompletableFuture<HttpResponse> end(Buffer data) {
        return end(data, null);
    }

    /**
     * Ends current request with given chunk data and obtains the response asynchronously.
     *
     * <strong>Please make sure the {@code data} won't be modified until
     * the {@code future} has completed, otherwise maybe the modified data will
     * be wrote to channel</strong>.
     *
     * @param data chunked data
     * @param handle which can be used to handle the result of end, if succeed, throwable will be null.
     * @return response
     */
    CompletableFuture<HttpResponse> end(Buffer data, Consumer<Throwable> handle);

    /**
     * Whether current request is allowed to write or not.
     *
     * @return      {@code true} or {@code false}.
     */
    boolean isWritable();

    @Override
    SegmentRequest enableUriEncode();

    @Override
    SegmentRequest disableExpectContinue();

    @Override
    SegmentRequest maxRedirects(int maxRedirects);

    @Override
    SegmentRequest maxRetries(int maxRetries);

    @Override
    SegmentRequest readTimeout(long readTimeout);

    @Override
    SegmentRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    SegmentRequest addParams(Map<String, String> params);

    @Override
    SegmentRequest handle(Consumer<Handle> handle);

    @Override
    SegmentRequest handler(Handler handler);

    @Override
    SegmentRequest addHeader(CharSequence name, CharSequence value);

    @Override
    SegmentRequest setHeader(CharSequence name, CharSequence value);

    @Override
    SegmentRequest removeHeader(CharSequence name);

    @Override
    SegmentRequest addParam(String name, String value);

    @Override
    SegmentRequest copy();

    @Override
    default boolean isSegmented() {
        return true;
    }
}
