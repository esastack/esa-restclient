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

import esa.commons.io.IOUtils;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.httpclient.core.Context;
import esa.httpclient.core.FileRequest;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedInput;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static esa.httpclient.core.util.HttpHeadersUtils.toHttp2Headers;

class FileWriter extends RequestWriterImpl<FileRequest> {

    private static final String MODE_READ = "r";
    private static final FileWriter INSTANCE = new FileWriter();

    private FileWriter() {
    }

    @Override
    public ChannelFuture writeAndFlush(FileRequest request,
                                       Channel channel,
                                       Context ctx,
                                       ChannelPromise headFuture,
                                       boolean uriEncodeEnabled,
                                       HttpVersion version,
                                       boolean http2) throws IOException {
        addContentLengthIfAbsent(request, v -> request.file() == null ? 0L : request.file().length());
        addContentTypeIfAbsent(request, () -> HttpHeaderValues.APPLICATION_OCTET_STREAM);

        return super.writeAndFlush(request, channel, ctx, headFuture, uriEncodeEnabled, version, http2);
    }

    @Override
    ChannelFuture writeAndFlush1(FileRequest request,
                                 Channel channel,
                                 Context context,
                                 ChannelPromise headFuture,
                                 HttpVersion version,
                                 boolean uriEncodeEnabled) {
        final DefaultHttpRequest req = new DefaultHttpRequest(version,
                HttpMethod.valueOf(request.method().name()),
                request.uri().relative(uriEncodeEnabled),
                ((Http1HeadersImpl) request.headers()));

        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger().debug("Send Request:\n" + req);
        }
        channel.write(req, headFuture);

        final ChannelPromise endPromise = channel.newPromise();
        // Write content
        if (writeContentNow(context)) {
            doWriteContent1(request, channel, endPromise);
        } else {
            channel.flush();
            ((NettyContext) context).set100ContinueCallback(()
                    -> doWriteContent1(request, channel, endPromise));
        }

        return endPromise;
    }

    private static void doWriteContent1(FileRequest request, Channel channel, ChannelPromise endPromise) {
        // Write content
        long length;
        RandomAccessFile rFile = null;
        ChunkedInput<ByteBuf> chunked = null;
        try {
            rFile = new RandomAccessFile(request.file(), MODE_READ);
            length = rFile.length();

            if (channel.pipeline().get(SslHandler.class) == null) {
                // SSL not enabled - can use zero-copy file transfer.
                // Note: FileChannel will be automatically closed once refCnt() returns 0
                channel.write(new DefaultFileRegion(rFile.getChannel(), 0, length));

                // Write the end marker.
                channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT, endPromise);
            } else {
                // SSL enabled - cannot use zero-copy file transfer.
                // Note: HttpChunkedInput will write the end marker (LastHttpContent) for us.
                channel.writeAndFlush(chunked = new ChunkedFile(rFile), endPromise);
            }

            // Try to close resource
            cleanAndLog(endPromise, chunked);
        } catch (IOException ex) {
            IOUtils.closeQuietly(rFile);
            endPromise.setFailure(ex);
        }
    }

    @Override
    ChannelFuture writeAndFlush2(FileRequest request,
                                 Channel channel,
                                 Context context,
                                 ChannelPromise headFuture,
                                 Http2ConnectionHandler handler,
                                 int streamId,
                                 boolean uriEncodeEnabled) {
        final ChannelFuture future = checkAndWriteH2Headers(channel,
                handler,
                toHttp2Headers(request, (Http1HeadersImpl) request.headers(), uriEncodeEnabled),
                streamId,
                false,
                headFuture);
        // Writes http2 headers
        if (future.isDone() && !future.isSuccess()) {
            return future;
        }

        final ChannelPromise endPromise = channel.newPromise();

        // Writes http2 content
        if (writeContentNow(context)) {
            Utils.runInChannel(channel, () -> {
                try {
                    doWriteContent2(request.file(),
                            channel,
                            streamId,
                            endPromise);
                } catch (IOException e) {
                    endPromise.setFailure(e);
                }
            });
        } else {
            channel.flush();
            ((NettyContext) context).set100ContinueCallback(() -> Utils.runInChannel(channel, () -> {
                try {
                    doWriteContent2(request.file(),
                            channel,
                            streamId,
                            endPromise);
                } catch (IOException ex) {
                    endPromise.setFailure(ex);
                }
            }));
        }

        return endPromise;
    }

    private static void doWriteContent2(File file,
                                        Channel channel,
                                        int streamId,
                                        ChannelPromise endPromise) throws IOException {
        // Writes http2 content
        ChunkedInput<ByteBuf> chunked = null;
        try {
            RandomAccessFile rFile = new RandomAccessFile(file, MODE_READ);
            chunked = new ChunkedFile(rFile);
            cleanAndLog(channel.writeAndFlush(new Http2ChunkedInput(chunked, streamId), endPromise), chunked);
        } catch (Throwable th) {
            final IOException ex;
            if (th instanceof IOException) {
                ex = (IOException) th;
            } else {
                ex = new IOException(th);
            }

            closeChunkedInputQuietly(chunked);
            throw ex;
        }
    }

    private static void cleanAndLog(ChannelFuture future, ChunkedInput<?> chunked) {
        if (future.isDone()) {
            closeChunkedInputQuietly(chunked);
        } else {
            future.addListener(f -> closeChunkedInputQuietly(chunked));
        }
    }

    private static void closeChunkedInputQuietly(ChunkedInput<?> chunkedInput) {
        if (chunkedInput != null) {
            try {
                chunkedInput.close();
            } catch (Exception ex) {
                LoggerUtils.logger().error("Error while closing chunked input", ex);
            }
        }
    }

    static FileWriter singleton() {
        return INSTANCE;
    }
}
