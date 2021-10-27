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

import esa.commons.http.HttpHeaderValues;
import io.esastack.httpclient.core.ExecContextUtil;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.exec.ExecContext;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.BDDAssertions.then;

class MultipartWriterTest extends Http2ConnectionHelper {

    private final HttpClient client = HttpClient.ofDefault();

    ////////*********************************HTTP1 MULTIPART WRITER**************************************////////

    @Test
    void testWriteAndFlushHttp1() throws IOException {
        final MultipartWriter writer = MultipartWriter.singleton();
        final EmbeddedChannel channel = new EmbeddedChannel();

        final File file = File.createTempFile("httpclient-", ".tmp");
        file.deleteOnExit();

        try {
            try (FileOutputStream out = new FileOutputStream(file)) {
                final byte[] data = new byte[4 * 1024 * 1024];
                ThreadLocalRandom.current().nextBytes(data);
                out.write(data);
            }

            final io.esastack.httpclient.core.MultipartRequest request = client
                    .post("http://127.0.0.1/abc")
                    .multipart()
                    .file("file", file, null, true)
                    .attr("key1", "value1");
            final ExecContext ctx = ExecContextUtil.newAs();
            final ChannelFuture end = writer.writeAndFlush(request,
                    channel,
                    ctx,
                    channel.newPromise(),
                    false,
                    HttpVersion.HTTP_1_1,
                    false);
            channel.flush();

            HttpRequest req = channel.readOutbound();
            then(req.method()).isSameAs(HttpMethod.POST);
            then(req.headers().get(esa.commons.http.HttpHeaderNames.CONTENT_TYPE)
                    .contains(esa.commons.http.HttpHeaderValues.MULTIPART_FORM_DATA)).isTrue();
            then(req.headers().get(HttpHeaderNames.HOST)).isEqualTo("127.0.0.1");
            then(req.protocolVersion()).isSameAs(HttpVersion.HTTP_1_1);

            HttpPostRequestEncoder chunked = channel.readOutbound();
            then(chunked).isNotNull();

            then(chunked.isChunked()).isTrue();
            then(chunked.isMultipart()).isTrue();
            then(chunked.getBodyListAttributes().size()).isEqualTo(2);
            then(end.isDone() && end.isSuccess()).isTrue();
        } finally {
            file.delete();
        }
    }

    @Test
    void testFormUrlEncoded1() throws IOException {
        final MultipartWriter writer = MultipartWriter.singleton();
        final EmbeddedChannel channel = new EmbeddedChannel();

        final io.esastack.httpclient.core.MultipartRequest request = client
                .post("http://127.0.0.1/abc")
                .multipart()
                .attr("key1", "value1")
                .attr("key2", "value2")
                .multipartEncode(false);
        final ExecContext ctx = ExecContextUtil.newAs();
        final ChannelFuture end = writer.writeAndFlush(request,
                channel,
                ctx,
                channel.newPromise(),
                false,
                HttpVersion.HTTP_1_1,
                false);
        channel.flush();

        HttpRequest req = channel.readOutbound();
        then(req.method()).isSameAs(HttpMethod.POST);
        then(req.headers().get(esa.commons.http.HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
        then(req.headers().get(HttpHeaderNames.HOST)).isEqualTo("127.0.0.1");
        then(req.protocolVersion()).isSameAs(HttpVersion.HTTP_1_1);

        LastHttpContent last = channel.readOutbound();
        then(last).isNotNull();
        then(end.isDone() && end.isSuccess()).isTrue();
    }

    @Test
    void test100ExpectContinue1() throws IOException {
        final MultipartWriter writer = MultipartWriter.singleton();
        final EmbeddedChannel channel = new EmbeddedChannel();

        final File file = File.createTempFile("httpclient-", ".tmp");
        file.deleteOnExit();

        try {
            try (FileOutputStream out = new FileOutputStream(file)) {
                final byte[] data = new byte[4 * 1024 * 1024];
                ThreadLocalRandom.current().nextBytes(data);
                out.write(data);
            }

            final io.esastack.httpclient.core.MultipartRequest request = client
                    .post("http://127.0.0.1/abc")
                    .multipart()
                    .file("file", file, null, true)
                    .attr("key1", "value1");
            request.headers().add(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE);

            final NettyExecContext ctx = ExecContextUtil.newAsNetty();
            final ChannelFuture end = writer.writeAndFlush(request,
                    channel,
                    ctx,
                    channel.newPromise(),
                    false,
                    HttpVersion.HTTP_1_1,
                    false);
            channel.flush();

            HttpRequest req = channel.readOutbound();
            then(req).isNotNull();
            then(req.method()).isSameAs(HttpMethod.POST);
            then(req.headers().get(esa.commons.http.HttpHeaderNames.CONTENT_TYPE)
                    .contains(esa.commons.http.HttpHeaderValues.MULTIPART_FORM_DATA)).isTrue();
            then(req.headers().get(HttpHeaderNames.HOST)).isEqualTo("127.0.0.1");
            then(req.protocolVersion()).isSameAs(HttpVersion.HTTP_1_1);

            HttpPostRequestEncoder chunked = channel.readOutbound();
            then(chunked).isNull();

            ctx.remove100ContinueCallback().run();

            chunked = channel.readOutbound();
            then(chunked.isChunked()).isTrue();
            then(chunked.isMultipart()).isTrue();
            then(chunked.getBodyListAttributes().size()).isEqualTo(2);
            then(end.isDone() && end.isSuccess()).isTrue();
        } finally {
            file.delete();
        }
    }

    @Test
    void testEncode1Error() throws Exception {
        final MultipartWriter writer = MultipartWriter.singleton();
        final EmbeddedChannel channel = new EmbeddedChannel();

        final File file = File.createTempFile("httpclient-", ".tmp");
        file.deleteOnExit();

        try {
            try (FileOutputStream out = new FileOutputStream(file)) {
                final byte[] data = new byte[4 * 1024 * 1024];
                ThreadLocalRandom.current().nextBytes(data);
                out.write(data);
            }

            final io.esastack.httpclient.core.MultipartRequest request = client
                    .post("http://127.0.0.1/abc")
                    .multipart()
                    .file("file", file, null, true)
                    .attr("key1", "value1");

            file.delete();
            final ExecContext ctx = ExecContextUtil.newAs();

            final ChannelFuture end = writer.writeAndFlush(request,
                    channel,
                    ctx,
                    channel.newPromise(),
                    false,
                    HttpVersion.HTTP_1_1,
                    false);
            channel.flush();

            Object req = channel.readOutbound();
            then(req).isNull();
            then(end.isDone()).isTrue();
            then(end.cause()).isInstanceOf(IOException.class);
        } finally {
            file.delete();
        }
    }

    ////////*********************************HTTP2 MULTIPART WRITER**************************************////////

    @Test
    void testWriteAndFlush2() throws Exception {
        setUp();
        final MultipartWriter writer = MultipartWriter.singleton();

        final File file = File.createTempFile("httpclient-", ".tmp");
        file.deleteOnExit();

        try {
            final byte[] data = new byte[4 * 1024 * 1024];
            try (FileOutputStream out = new FileOutputStream(file)) {
                ThreadLocalRandom.current().nextBytes(data);
                out.write(data);
            }

            final io.esastack.httpclient.core.MultipartRequest request = client
                    .post("http://127.0.0.1/abc")
                    .multipart()
                    .file("file", file, null, true);
            final ExecContext ctx = ExecContextUtil.newAs();
            request.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), STREAM_ID);

            final ChannelFuture end = writer.writeAndFlush(request,
                    channel,
                    ctx,
                    channel.newPromise(),
                    false,
                    null,
                    true);
            channel.flush();
            // Ignore preface
            channel.readOutbound();

            Helper.HeaderFrame header = channel.readOutbound();
            then(header).isNotNull();
            then(header.streamId).isEqualTo(STREAM_ID);
            then(header.headers.method()).isEqualTo(HttpMethod.POST.asciiName());
            then(header.headers.get(HttpHeaderNames.CONTENT_TYPE).toString()
                    .contains(esa.commons.http.HttpHeaderValues.MULTIPART_FORM_DATA)).isTrue();
            then(header.headers.authority().toString()).isEqualTo("127.0.0.1");

            int dataCount = 0;
            while ((channel.readOutbound()) != null) {
                dataCount++;
            }

            then(dataCount > data.length / 8192).isTrue();
            then(end.isDone() && end.isSuccess()).isTrue();
        } finally {
            file.delete();
        }
    }

    @Test
    void testFormUrlEncoded2() throws Exception {
        setUp();
        final MultipartWriter writer = MultipartWriter.singleton();

        final io.esastack.httpclient.core.MultipartRequest request = client
                .post("http://127.0.0.1/abc")
                .multipart()
                .attr("key1", "value1")
                .attr("key2", "value2")
                .multipartEncode(false);

        final ExecContext ctx = ExecContextUtil.newAs();
        request.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), STREAM_ID);

        final ChannelFuture end = writer.writeAndFlush(request,
                channel,
                ctx,
                channel.newPromise(),
                false,
                null,
                true);
        channel.flush();
        // Ignore preface
        channel.readOutbound();

        Helper.HeaderFrame header = channel.readOutbound();
        then(header).isNotNull();
        then(header.streamId).isEqualTo(STREAM_ID);
        then(header.headers.method()).isEqualTo(HttpMethod.POST.asciiName());
        then(header.headers.get(HttpHeaderNames.CONTENT_TYPE).toString())
                .isEqualTo(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
        then(header.headers.authority().toString()).isEqualTo("127.0.0.1");

        Helper.DataFrame frame = channel.readOutbound();
        then(frame).isNotNull();
        then(frame.endStream).isTrue();
        then(end.isDone() && end.isSuccess()).isTrue();
    }

    @Test
    void test100ExpectContinue2() throws Exception {
        setUp();
        final MultipartWriter writer = MultipartWriter.singleton();

        final File file = File.createTempFile("httpclient-", ".tmp");
        file.deleteOnExit();

        try {
            final byte[] data = new byte[4 * 1024 * 1024];
            try (FileOutputStream out = new FileOutputStream(file)) {
                ThreadLocalRandom.current().nextBytes(data);
                out.write(data);
            }

            final io.esastack.httpclient.core.MultipartRequest request = client
                    .post("http://127.0.0.1/abc")
                    .multipart()
                    .file("file", file, null, true);
            request.headers().add(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE);

            final NettyExecContext ctx = ExecContextUtil.newAsNetty();
            request.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), STREAM_ID);

            final ChannelFuture end = writer.writeAndFlush(request,
                    channel,
                    ctx,
                    channel.newPromise(),
                    false,
                    null,
                    true);
            channel.flush();
            // Ignore preface
            channel.readOutbound();

            Helper.HeaderFrame header = channel.readOutbound();
            then(header).isNotNull();
            then(header.streamId).isEqualTo(STREAM_ID);
            then(header.headers.method()).isEqualTo(HttpMethod.POST.asciiName());
            then(header.headers.get(HttpHeaderNames.CONTENT_TYPE).toString()
                    .contains(esa.commons.http.HttpHeaderValues.MULTIPART_FORM_DATA)).isTrue();
            then(header.headers.authority().toString()).isEqualTo("127.0.0.1");

            Object chunked = channel.readOutbound();
            then(chunked).isNull();

            ctx.remove100ContinueCallback().run();

            int dataCount = 0;
            while ((channel.readOutbound()) != null) {
                dataCount++;
            }

            then(dataCount > data.length / 8192).isTrue();
            then(end.isDone() && end.isSuccess()).isTrue();
        } finally {
            file.delete();
        }
    }

    @Test
    void testEncode2Error() throws Exception {
        setUp();
        final MultipartWriter writer = MultipartWriter.singleton();

        final File file = File.createTempFile("httpclient-", ".tmp");
        file.deleteOnExit();

        try {
            try (FileOutputStream out = new FileOutputStream(file)) {
                final byte[] data = new byte[4 * 1024 * 1024];
                ThreadLocalRandom.current().nextBytes(data);
                out.write(data);
            }

            final io.esastack.httpclient.core.MultipartRequest request = client
                    .post("http://127.0.0.1/abc")
                    .multipart()
                    .file("file", file, null, true);

            final ExecContext ctx = ExecContextUtil.newAs();
            request.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), STREAM_ID);
            file.delete();

            final ChannelFuture end = writer.writeAndFlush(request,
                    channel,
                    ctx,
                    channel.newPromise(),
                    false,
                    null,
                    true);
            channel.flush();
            // Ignore preface
            channel.readOutbound();

            Object obj = channel.readOutbound();
            then(obj).isNull();

            then(end.isDone()).isTrue();
            then(end.isSuccess()).isFalse();
            then(end.cause()).isInstanceOf(IOException.class);
        } finally {
            file.delete();
        }
    }

}
