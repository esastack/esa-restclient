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

import esa.commons.http.HttpHeaders;
import esa.commons.netty.core.Buffer;
import esa.commons.netty.core.BufferImpl;
import esa.commons.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.SegmentRequest;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SegmentWriterTest extends Http2ConnectionHelper {

    ////////*********************************HTTP1 CHUNK WRITER**************************************////////

    @Test
    void testWriteAndFlush1() throws IOException {
        final SegmentWriter writer = new SegmentWriter();
        final EmbeddedChannel channel = new EmbeddedChannel();
        final HttpClient client = HttpClient.ofDefault();

        final SegmentRequest request = client.post("http://127.0.0.1/abc").segment();
        final Context ctx = new Context();
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
        then(req.headers().get(HttpHeaderNames.TRANSFER_ENCODING)).isEqualTo(HttpHeaderValues.CHUNKED.toString());
        then(req.headers().get(HttpHeaderNames.HOST)).isEqualTo("127.0.0.1");
        then(req.protocolVersion()).isSameAs(HttpVersion.HTTP_1_1);

        then(end.isDone()).isFalse();
        // Write byte[]
        final byte[] data = new byte[1024];
        ThreadLocalRandom.current().nextBytes(data);
        writer.write(data, 0, data.length);
        HttpContent content = channel.readOutbound();
        then(content.content().readableBytes()).isEqualTo(data.length);

        // Write Buffer
        Buffer buffer = new BufferImpl(data.length);
        buffer.writeBytes(data);
        writer.write(buffer, -1, -1);
        content = channel.readOutbound();
        then(content.content().readableBytes()).isEqualTo(data.length);

        // Error on unsupported message
        ChannelFuture unsupportedData = writer.write(new Object(), -1, -1);
        content = channel.readOutbound();
        then(content).isNull();
        then(unsupportedData.isDone()).isTrue();
        then(unsupportedData.cause()).isInstanceOf(IllegalArgumentException.class);

        // End with trailers
        HttpHeaders headers = new Http1HeadersImpl();
        headers.add("a", "b");
        headers.add("x", "y");
        writer.end(headers);
        LastHttpContent last = channel.readOutbound();
        then(last.content().readableBytes()).isEqualTo(0);
        then(last.trailingHeaders().get("a")).isEqualTo("b");
        then(last.trailingHeaders().get("x")).isEqualTo("y");
        then(end.isDone()).isTrue();

        // Error after ended
        ChannelFuture ended = writer.end(null);
        then(ended.isDone()).isTrue();
        then(ended.cause()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testEndH1WithEmptyTrailers() throws IOException {
        final SegmentWriter writer = new SegmentWriter();
        final EmbeddedChannel channel = new EmbeddedChannel();
        final HttpClient client = HttpClient.ofDefault();

        final SegmentRequest request = client.post("http://127.0.0.1/abc").segment();
        final Context ctx = new Context();
        final ChannelFuture end = writer.writeAndFlush(request,
                channel,
                ctx,
                channel.newPromise(),
                false,
                HttpVersion.HTTP_1_1,
                false);
        channel.flush();
        channel.readOutbound();

        writer.end(null);
        LastHttpContent last = channel.readOutbound();
        then(last.content().readableBytes()).isEqualTo(0);
        then(last.trailingHeaders().isEmpty()).isTrue();
        then(end.isDone()).isTrue();
    }

    ////////*********************************HTTP2 CHUNK WRITER**************************************////////

    @Test
    void testWriteAndFlush2() throws Exception {
        setUp();
        final SegmentWriter writer = new SegmentWriter();

        final HttpClient client = HttpClient.ofDefault();
        final SegmentRequest request = client.post("http://127.0.0.1/abc").segment();
        final Context ctx = new Context();
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
        then(header.headers.get(HttpHeaderNames.TRANSFER_ENCODING)).isNull();
        then(header.headers.authority().toString()).isEqualTo("127.0.0.1");
        then(end.isDone()).isFalse();

        // Write byte[]
        final byte[] data = new byte[1024];
        ThreadLocalRandom.current().nextBytes(data);
        writer.write(data, 0, data.length);
        Helper.DataFrame content = channel.readOutbound();
        then(content.data.readableBytes()).isEqualTo(data.length);
        then(content.endStream).isFalse();

        // Write Buffer
        Buffer buffer = new BufferImpl(data.length);
        buffer.writeBytes(data);
        writer.write(buffer, -1, -1);
        content = channel.readOutbound();
        then(content.data.readableBytes()).isEqualTo(data.length);
        then(content.endStream).isFalse();

        // Error on unsupported message
        ChannelFuture unsupportedData = writer.write(new Object(), -1, -1);
        content = channel.readOutbound();
        then(content).isNull();
        then(unsupportedData.isDone()).isTrue();
        then(unsupportedData.cause()).isInstanceOf(IllegalArgumentException.class);

        // End with trailers
        HttpHeaders headers = new Http1HeadersImpl();
        headers.add("a", "b");
        headers.add("x", "y");
        writer.end(headers);
        Helper.HeaderFrame last = channel.readOutbound();
        then(last.headers.get("a")).isEqualTo("b");
        then(last.headers.get("x")).isEqualTo("y");
        then(end.isDone()).isTrue();

        // Error after ended
        ChannelFuture ended = writer.end(null);
        then(ended.isDone()).isTrue();
        then(ended.cause()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testEndH2WithEmptyData() throws Exception {
        setUp();
        final SegmentWriter writer = new SegmentWriter();
        final HttpClient client = HttpClient.ofDefault();

        final SegmentRequest request = client.post("http://127.0.0.1/abc").segment();
        final Context ctx = new Context();
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

        // Ignore header frame
        channel.readOutbound();

        writer.end();
        Helper.DataFrame last  = channel.readOutbound();
        then(last.data.readableBytes()).isEqualTo(0);
        then(last.endStream).isTrue();
        then(end.isDone()).isTrue();
    }

    @Test
    void testWriteError() throws Exception {
        setUp();
        final SegmentWriter writer = new SegmentWriter();
        final HttpClient client = HttpClient.ofDefault();

        final SegmentRequest request = client.post("http://127.0.0.1/abc").segment();
        final Context ctx = new Context();
        request.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), STREAM_ID);

        writer.writeAndFlush(request,
                channel,
                ctx,
                channel.newPromise(),
                false,
                null,
                true);
        channel.flush();

        assertThrows(IllegalArgumentException.class, () -> writer.write("Hello".getBytes(), -1, -1));
    }

    @Test
    void testClose() throws Exception {
        setUp();
        final SegmentWriter writer = new SegmentWriter();
        final HttpClient client = HttpClient.ofDefault();

        final SegmentRequest request = client.post("http://127.0.0.1/abc").segment();
        final Context ctx = new Context();
        request.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), STREAM_ID);

        final ChannelFuture end = writer.writeAndFlush(request,
                channel,
                ctx,
                channel.newPromise(),
                false,
                null,
                true);

        writer.close(new RuntimeException());
        then(end.isDone()).isTrue();
        then(end.cause()).isInstanceOf(RuntimeException.class);
    }

}
