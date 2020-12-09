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

import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpHeaderValues;
import esa.httpclient.core.Context;
import esa.httpclient.core.ContextImpl;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpRequestBuilder;
import esa.httpclient.core.exception.StreamIdExhaustedException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ThreadLocalRandom;

import static esa.httpclient.core.ContextNames.EXPECT_CONTINUE_ENABLED;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestWriterImplTest {

    @SuppressWarnings("unchecked")
    @Test
    void testWriteAndFlush() throws IOException {
        final RequestWriterImpl writer = new FakeRequestWriterImpl();
        final EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(mock(Http2ConnectionHandler.class));

        // Case 1: Host is present
        final HttpRequestBuilder.BodyForbiddenBuilder builder = HttpRequest.get("http://127.0.0.1:8080/abc");
        builder.addHeader(HttpHeaderNames.HOST, "127.0.0.1:8080");
        builder.addHeader(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), "3");
        final HttpRequest request1 = builder.build();

        ChannelFuture future = writer.writeAndFlush(request1,
                channel,
                mock(Context.class),
                ThreadLocalRandom.current().nextBoolean(),
                HttpVersion.HTTP_1_1,
                ThreadLocalRandom.current().nextBoolean());

        then(future.isSuccess()).isTrue();
        then(request1.headers().get(HttpHeaderNames.HOST)).isEqualTo("127.0.0.1:8080");

        // Case 2: Host is absent
        final HttpRequest request2 = HttpRequest.get("http://localhost:8080/abc").build();
        request2.addHeader(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), "3");

        future = writer.writeAndFlush(request2,
                channel,
                mock(Context.class),
                ThreadLocalRandom.current().nextBoolean(),
                HttpVersion.HTTP_1_1,
                ThreadLocalRandom.current().nextBoolean());
        then(future.isSuccess()).isTrue();
        then(request2.headers().get(HttpHeaderNames.HOST)).isEqualTo("localhost:8080");
    }

    @Test
    void testCheckAndWriteH2Headers() {
        final RequestWriterImpl writer = new FakeRequestWriterImpl();

        final Channel channel = new EmbeddedChannel();
        final Http2ConnectionHandler handler = mock(Http2ConnectionHandler.class);
        final ChannelPromise promise1 = channel.newPromise();

        // Illegal streamId
        when(handler.writeGoAwayOnExhaustion(promise1)).thenReturn(null);
        final ChannelFuture result1 = writer.checkAndWriteH2Headers(channel,
                handler,
                new DefaultHttp2Headers(),
                -1,
                true,
                promise1);
        then(result1.cause()).isInstanceOf(StreamIdExhaustedException.class);

        // legal streamId
        final ChannelPromise promise2 = channel.newPromise();
        final Http2Headers headers = new DefaultHttp2Headers();
        final boolean endOfStream = ThreadLocalRandom.current().nextBoolean();

        when(handler.writeHeaders(3,
                headers,
                endOfStream,
                promise2)).thenReturn(channel.newFailedFuture(new IOException("ABC")));
        final ChannelFuture result2 = writer.checkAndWriteH2Headers(channel,
                handler,
                headers,
                3,
                endOfStream,
                promise2);
        then(result2.cause().getMessage()).isEqualTo("ABC");
    }

    @Test
    void testWriteContentNow() {
        final ContextImpl ctx = new ContextImpl();
        then(RequestWriterImpl.writeContentNow(ctx)).isTrue();

        ctx.setAttr(EXPECT_CONTINUE_ENABLED, true);
        then(RequestWriterImpl.writeContentNow(ctx)).isFalse();

        ctx.setAttr(EXPECT_CONTINUE_ENABLED, false);
        then(RequestWriterImpl.writeContentNow(ctx)).isTrue();

        ctx.clear();
    }

    @Test
    void testAddContentLengthIfAbsent() {
        // Case 1: Content-Length is present
        final HttpRequest request1 = HttpRequest.get("/abc")
                .addHeader(HttpHeaderNames.CONTENT_LENGTH, "100")
                .build();

        RequestWriterImpl.addContentLengthIfAbsent(request1, (request) -> 200L);
        then(request1.headers().getLong(HttpHeaderNames.CONTENT_LENGTH)).isEqualTo(100L);

        // Case 2: Content-Length is present
        final HttpRequest request2 = HttpRequest.get("/abc").build();
        RequestWriterImpl.addContentLengthIfAbsent(request2, (request) -> 200L);
        then(request2.headers().getLong(HttpHeaderNames.CONTENT_LENGTH)).isEqualTo(200L);
    }

    @Test
    void testAddContentTypeIfAbsent() {
        // Case 1: Content-Type is present
        final HttpRequest request1 = HttpRequest.get("/abc")
                .addHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                .build();

        RequestWriterImpl.addContentTypeIfAbsent(request1, () -> HttpHeaderValues.APPLICATION_OCTET_STREAM);
        then(request1.headers().get(HttpHeaderNames.CONTENT_TYPE)).isEqualTo(HttpHeaderValues.APPLICATION_JSON);

        // Case 2: Content-Length is present
        final HttpRequest request2 = HttpRequest.get("/abc").build();
        RequestWriterImpl.addContentTypeIfAbsent(request2, () -> HttpHeaderValues.APPLICATION_OCTET_STREAM);
        then(request2.headers().get(HttpHeaderNames.CONTENT_TYPE)).isEqualTo(HttpHeaderValues
                .APPLICATION_OCTET_STREAM);
    }

    @Test
    void testComputeHost() {
        final URI uri1 = URI.create("http://127.0.0.1/abc");
        then(RequestWriterImpl.computeHost(uri1)).isEqualTo("127.0.0.1");

        final URI uri2 = URI.create("http://127.0.0.1:80/abc");
        then(RequestWriterImpl.computeHost(uri2)).isEqualTo("127.0.0.1");

        final URI uri3 = URI.create("https://127.0.0.1:443/abc");
        then(RequestWriterImpl.computeHost(uri3)).isEqualTo("127.0.0.1");

        final URI uri4 = URI.create("http://127.0.0.1:443/abc");
        then(RequestWriterImpl.computeHost(uri4)).isEqualTo("127.0.0.1:443");

        final URI uri5 = URI.create("https://127.0.0.1:80/abc");
        then(RequestWriterImpl.computeHost(uri5)).isEqualTo("127.0.0.1:80");
    }

    private class FakeRequestWriterImpl extends RequestWriterImpl {

        @Override
        ChannelFuture writeAndFlush2(HttpRequest request,
                                     Channel channel,
                                     Context context,
                                     Http2ConnectionHandler handler,
                                     int streamId,
                                     boolean uriEncodeEnabled) {
            return channel.newSucceededFuture();
        }

        @Override
        ChannelFuture writeAndFlush1(HttpRequest request,
                                     Channel channel,
                                     Context context,
                                     HttpVersion version,
                                     boolean uriEncodeEnabled) {
            return channel.newSucceededFuture();
        }
    }
}
