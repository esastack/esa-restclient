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

import esa.commons.collection.MultiValueMap;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.httpclient.core.Context;
import esa.httpclient.core.MultipartFileItem;
import esa.httpclient.core.MultipartRequest;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.stream.ChunkedInput;
import io.netty.util.internal.SystemPropertyUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static esa.httpclient.core.util.HttpHeadersUtils.toHttp2Headers;

class MultipartWriter extends RequestWriterImpl<MultipartRequest> {

    private static final String MEMORY_THRESHOLD_KEY = "esa.httpclient.multipart.memoryThreshold";
    private static final String TEMP_DIR_KEY = "esa.httpclient.multipart.tempDir";

    private static final long MEMORY_THRESHOLD = SystemPropertyUtil.getLong(MEMORY_THRESHOLD_KEY,
            2 * 1014 * 1024);

    private static final String TEMP_DIR = SystemPropertyUtil.get(TEMP_DIR_KEY);

    private static final MultipartWriter INSTANCE = new MultipartWriter();

    private static final HttpDataFactory FACTORY;

    static {
        // setup the factory: here using a mixed memory/disk based on size threshold
        // Disk if minSize exceed
        FACTORY = new DefaultHttpDataFactory(MEMORY_THRESHOLD);

        // should delete file on exit (in normal exit)
        DiskFileUpload.deleteOnExitTemporaryFile = true;

        // system temp directory
        DiskFileUpload.baseDirectory = TEMP_DIR;

        // should delete file on exit (in normal exit)
        DiskAttribute.deleteOnExitTemporaryFile = true;

        // system temp directory
        DiskAttribute.baseDirectory = TEMP_DIR;
    }

    @Override
    ChannelFuture writeAndFlush1(MultipartRequest request,
                                 Channel channel,
                                 Context context,
                                 ChannelPromise headFuture,
                                 HttpVersion version,
                                 boolean uriEncodeEnabled) {
        // Prepare the HTTP request.
        final HttpRequest request0 = new DefaultHttpRequest(version,
                HttpMethod.POST,
                request.uri().relative(uriEncodeEnabled),
                ((Http1HeadersImpl) request.headers()));

        final ChannelPromise endPromise = channel.newPromise();
        final Runnable runnable = () -> {
            try {
                encodeAndWrite1(request, request0, channel, context, headFuture, endPromise);
            } catch (IOException e) {
                endPromise.setFailure(e);
            }
        };

        Utils.runInChannel(channel, runnable);
        return endPromise;
    }

    private static void encodeAndWrite1(MultipartRequest request,
                                        HttpRequest request0,
                                        Channel channel,
                                        Context ctx,
                                        ChannelPromise headFuture,
                                        ChannelPromise endPromise) throws IOException {
        try {
            final HttpPostRequestEncoder encoder = buildEncoder(request0, request);

            // Finalize request
            final HttpRequest finalizedRequest = encoder.finalizeRequest();

            // Considering 100-expect-continue, We must write request immediately.
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger().debug("Send Request:\n" + finalizedRequest.headers());
            }
            channel.write(request0, headFuture);

            final Runnable writeContent = () -> {
                if (encoder.isChunked()) {
                    // Write content chunk by chunk
                    channel.writeAndFlush(encoder, endPromise);
                } else {
                    // FullHttpRequest
                    LastHttpContent last = new DefaultLastHttpContent(((FullHttpRequest) finalizedRequest).content());
                    last.trailingHeaders().add(((FullHttpRequest) finalizedRequest).trailingHeaders());
                    channel.writeAndFlush(last, endPromise);
                }
            };
            if (writeContentNow(ctx)) {
                writeContent.run();
            } else {
                channel.flush();
                ((NettyContext) ctx).set100ContinueCallback(() -> Utils.runInChannel(channel, writeContent));
            }

            // Now no more use of file representation (and list of HttpData)
            doClean(endPromise, encoder);
        } catch (Exception ex) {
            FACTORY.cleanRequestHttpData(request0);
            throw new IOException(ex);
        }
    }

    @Override
    ChannelFuture writeAndFlush2(MultipartRequest request,
                                 Channel channel,
                                 Context context,
                                 ChannelPromise headFuture,
                                 Http2ConnectionHandler handler,
                                 int streamId,
                                 boolean uriEncodeEnabled) {
        final HttpRequest request0 = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.valueOf(request.method().name()),
                request.uri().relative(uriEncodeEnabled),
                ((Http1HeadersImpl) request.headers()));

        final ChannelPromise endPromise = channel.newPromise();
        final Runnable runnable = () -> {
            try {
                encodeAndWrite2(request,
                        channel,
                        handler,
                        streamId,
                        request0,
                        context,
                        uriEncodeEnabled,
                        headFuture,
                        endPromise);
            } catch (IOException e) {
                endPromise.setFailure(e);
            }
        };

        Utils.runInChannel(channel, runnable);
        return endPromise;
    }

    private void encodeAndWrite2(MultipartRequest request,
                                 Channel channel,
                                 Http2ConnectionHandler handler,
                                 int streamId,
                                 HttpRequest request0,
                                 Context ctx,
                                 boolean uriEncodeEnabled,
                                 ChannelPromise headFuture,
                                 ChannelPromise endPromise) throws IOException {
        try {
            final HttpPostRequestEncoder encoder = buildEncoder(request0, request);
            final HttpRequest finalizedRequest = encoder.finalizeRequest();

            final ChannelFuture future = checkAndWriteH2Headers(channel,
                    handler,
                    // Note that request.headers is same as request.headers.
                    toHttp2Headers(request, (Http1HeadersImpl) request.headers(), uriEncodeEnabled),
                    streamId,
                    false,
                    headFuture);

            if (future.isDone() && !future.isSuccess()) {
                endPromise.setFailure(future.cause());
                return;
            }

            final Runnable writeContent = () -> {
                // case 1: FullHttpRequest
                if (!encoder.isChunked()) {
                    handler.writeData(streamId,
                            ((FullHttpRequest) finalizedRequest).content(),
                            true,
                            endPromise);
                    channel.flush();
                } else {
                    // case 2: chunked data
                    channel.writeAndFlush(new Http2ChunkedInput(new HttpContentToByteBuf(encoder), streamId),
                            endPromise);
                }
            };

            // Considering 100-expect-continue, We must write request immediately.
            if (!writeContentNow(ctx)) {
                channel.flush();
                ((NettyContext) ctx).set100ContinueCallback(() -> Utils.runInChannel(channel, writeContent));
            } else {
                writeContent.run();
            }

            // Now no more use of file representation (and list of HttpData)
            doClean(endPromise, encoder);
        } catch (Exception ex) {
            FACTORY.cleanRequestHttpData(request0);
            throw new IOException(ex);
        }
    }

    private static void doClean(ChannelPromise promise, HttpPostRequestEncoder encoder) {
        if (promise.isDone()) {
            try {
                encoder.cleanFiles();
            } catch (Throwable throwable) {
                LoggerUtils.logger().error("Failed to clean multipart items", throwable);
            }
        } else {
            promise.addListener(f -> {
                try {
                    encoder.cleanFiles();
                } catch (Throwable throwable) {
                    LoggerUtils.logger().error("Failed to clean multipart items", throwable);
                }
            });
        }
    }

    private static HttpPostRequestEncoder buildEncoder(HttpRequest request0, MultipartRequest request)
            throws HttpPostRequestEncoder.ErrorDataEncoderException {
        final HttpPostRequestEncoder encoder = new HttpPostRequestEncoder(FACTORY,
                request0,
                request.multipartEncode());

        final MultiValueMap<String, String> attributes = request.attrs();
        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            for (String value : entry.getValue()) {
                encoder.addBodyAttribute(entry.getKey(), value);
            }
        }

        for (MultipartFileItem item : request.files()) {
            encoder.addBodyFileUpload(item.name(),
                    item.fileName(),
                    item.file(),
                    item.contentType(),
                    item.isText());
        }

        return encoder;
    }

    private MultipartWriter() {
    }

    static MultipartWriter singleton() {
        return INSTANCE;
    }

    private static final class HttpContentToByteBuf implements ChunkedInput<ByteBuf> {

        private final ChunkedInput<HttpContent> delegating;

        private HttpContentToByteBuf(ChunkedInput<HttpContent> delegating) {
            this.delegating = delegating;
        }

        @Override
        public boolean isEndOfInput() throws Exception {
            return delegating.isEndOfInput();
        }

        @Override
        public void close() throws Exception {
            delegating.close();
        }

        @Deprecated
        @Override
        public ByteBuf readChunk(ChannelHandlerContext ctx) throws Exception {
            return readChunk(ctx.alloc());
        }

        @Override
        public ByteBuf readChunk(ByteBufAllocator allocator) throws Exception {
            HttpContent content = delegating.readChunk(allocator);
            if (content == null) {
                return Unpooled.EMPTY_BUFFER;
            }

            return content.content();
        }

        @Override
        public long length() {
            return delegating.length();
        }

        @Override
        public long progress() {
            return delegating.progress();
        }
    }
}
