/*
 * Copyright 2021 OPPO ESA Stack Project
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
package esa.httpclient.core;

import esa.commons.http.HttpHeaderValues;
import esa.commons.http.HttpMethod;
import esa.commons.netty.core.BufferImpl;
import esa.httpclient.core.netty.NettyHttpClient;
import esa.httpclient.core.util.Futures;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;

import static org.assertj.core.api.Java6BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompositeRequestTest {

    @Test
    void testBasic() {
        final String uri = "http://127.0.0.1:8080/abc";
        final HttpClientBuilder builder = HttpClient.create();
        final NettyHttpClient client = mock(NettyHttpClient.class);
        final HttpMethod method = HttpMethod.PUT;
        final SegmentRequest chunk0 = mock(SegmentRequest.class);

        final CompositeRequest request = new CompositeRequest(builder,
                client, () -> chunk0, method, uri);

        final MultipartRequest multipart = request.multipart();
        then(multipart.isMultipart()).isTrue();
        then(multipart.isSegmented()).isFalse();
        then(multipart.buffer()).isNull();
        then(multipart.file()).isNull();
        then(multipart.attrs()).isEmpty();
        then(multipart.files()).isEmpty();

        multipart.attr("a", "b");
        multipart.attr("x", "y");
        multipart.attr("xxxxx", "mmmmmmm");

        final File file = new File("/abc");
        multipart.file("a1", file);
        multipart.file("a2", file, HttpHeaderValues.APPLICATION_OCTET_STREAM);
        multipart.file("a3", file, "xxx", true);
        multipart.file("a4", "file1", file, HttpHeaderValues.TEXT_PLAIN, true);
        multipart.file("xxxx", new File("/def"), "mmm", true);
        then(multipart.multipartEncode()).isTrue();
        multipart.multipartEncode(false);
        then(multipart.multipartEncode()).isFalse();

        then(request.attrs().size()).isEqualTo(3);
        then(request.files().size()).isEqualTo(5);

        assertThrows(IllegalStateException.class, request::segment);

        assertThrows(IllegalStateException.class, () -> request.body(new File("")));
        then(new CompositeRequest(builder, client, () -> chunk0, method, uri).body(new File(""))
                .file()).isNotNull();

        assertThrows(IllegalStateException.class, () -> request.body(new BufferImpl().writeBytes("Hello".getBytes())));
        then(new CompositeRequest(builder, client, () -> chunk0, method, uri)
                .body(new BufferImpl().writeBytes("Hello".getBytes()))).isNotNull();
    }

    @Test
    void testType() {
        final String uri = "http://127.0.0.1:8080/abc";
        final HttpClientBuilder builder = HttpClient.create();
        final NettyHttpClient client = mock(NettyHttpClient.class);
        when(client.execute(any(HttpRequest.class), any(Context.class), any(), any()))
                .thenReturn(Futures.completed());
        final HttpMethod method = HttpMethod.PUT;
        final SegmentRequest chunk0 = mock(SegmentRequest.class);

        final CompositeRequest request0 = new CompositeRequest(builder,
                client, () -> chunk0, method, uri);
        then(request0.isMultipart()).isFalse();
        then(request0.isFile()).isFalse();
        then(request0.isMultipart()).isFalse();

        request0.multipart();
        then(request0.isMultipart()).isTrue();
        then(request0.isFile()).isFalse();
        then(request0.isSegmented()).isFalse();

        final CompositeRequest request1 = new CompositeRequest(builder,
                client, () -> chunk0, method, uri);
        request1.body(mock(File.class));
        then(request1.isMultipart()).isFalse();
        then(request1.isFile()).isTrue();
        then(request1.isSegmented()).isFalse();
    }

    @Test
    void testCopy() throws Exception {
        final String uri = "http://127.0.0.1:8080/abc";
        final HttpClientBuilder builder = HttpClient.create();
        final NettyHttpClient client = mock(NettyHttpClient.class);
        when(client.execute(any(HttpRequest.class), any(Context.class), any(), any()))
                .thenReturn(Futures.completed());
        final HttpMethod method = HttpMethod.PUT;
        final SegmentRequest chunk0 = mock(SegmentRequest.class);
        final boolean multipartEncode = true;

        final CompositeRequest request = new CompositeRequest(builder,
                client, () -> chunk0, method, uri);

        final MultipartRequest multipart = request.multipart();
        multipart.attr("a", "b");
        multipart.attr("x", "y");
        multipart.attr("xxxxx", "mmmmmmm");

        final File file = new File("/abc");
        multipart.file("a1", file);
        multipart.file("a2", file, HttpHeaderValues.APPLICATION_OCTET_STREAM);
        multipart.file("a3", file, "xxx", true);
        multipart.file("a4", "file1", file, HttpHeaderValues.TEXT_PLAIN, true);
        multipart.file("xxxx", new File("/def"), "mmm", true);
        multipart.multipartEncode(multipartEncode);

        then(request.attrs().size()).isEqualTo(3);
        then(request.files().size()).isEqualTo(5);

        final CompositeRequest copied = request.copy();
        then(copied.multipartEncode()).isEqualTo(multipartEncode);
        then(copied.attrs().size()).isEqualTo(3);
        then(copied.files().size()).isEqualTo(5);
        then(copied.file()).isNull();
        then(copied.buffer()).isNull();
        then(copied.isSegmented()).isFalse();
        then(copied.isFile()).isFalse();
        then(copied.isMultipart()).isTrue();

        // Test isolation
        request.file("m", file);
        request.attr("mm", "nn");
        then(request.attrs().size()).isEqualTo(4);
        then(request.files().size()).isEqualTo(6);
        then(copied.attrs().size()).isEqualTo(3);
        then(copied.files().size()).isEqualTo(5);

        then(request.execute().get()).isNull();
        assertThrows(IllegalStateException.class, request::execute);

        then(copied.execute().get()).isNull();
        assertThrows(IllegalStateException.class, copied::execute);
    }

    @Test
    void testUnmodifiableAfterStarted() {
        final String uri = "http://127.0.0.1:8080/abc";
        final HttpClientBuilder builder = HttpClient.create();
        final NettyHttpClient client = mock(NettyHttpClient.class);
        final HttpMethod method = HttpMethod.PUT;
        final SegmentRequest chunk0 = mock(SegmentRequest.class);
        final byte[] data = "Hello".getBytes();

        final CompositeRequest request = new CompositeRequest(builder,
                client, () -> chunk0, method, uri);
        when(client.execute(any(), any(), any(), any()))
                .thenReturn(Futures.completed(mock(HttpResponse.class)));

        // Before writing
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

        final File file = new File("/abc");

        request.file("a1", file);
        request.file("a2", file, HttpHeaderValues.APPLICATION_OCTET_STREAM);
        request.file("a3", file, "xxx", true);
        request.file("a4", "file1", file, HttpHeaderValues.TEXT_PLAIN, true);
        request.attr("a", "b");
        request.multipartEncode(true);
        request.body(new BufferImpl().writeBytes(data));
        assertThrows(IllegalStateException.class, () -> request.body(file));

        request.execute();

        // After writing
        // Header ops are allowed
        request.addHeaders(Collections.singletonMap("a", "b"));
        request.addHeader("x", "y");
        request.setHeader("a", "bb");
        request.removeHeader("a");

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

        assertThrows(IllegalStateException.class, () -> request.file("a1", file));
        assertThrows(IllegalStateException.class,
                () -> request.file("a2", file, HttpHeaderValues.APPLICATION_OCTET_STREAM));
        assertThrows(IllegalStateException.class,
                () -> request.file("a3", file, "xxx", true));
        assertThrows(IllegalStateException.class,
                () -> request.file("a4", "file1", file, HttpHeaderValues.TEXT_PLAIN, true));
        assertThrows(IllegalStateException.class, () -> request.attr("a", "b"));
        assertThrows(IllegalStateException.class, () -> request.multipartEncode(true));
        assertThrows(IllegalStateException.class, () -> request.body(file));
        assertThrows(IllegalStateException.class, () -> request.body(new BufferImpl().writeBytes(data)));

        assertThrows(IllegalStateException.class, request::execute);
    }


    @Test
    void testSegmentRequest() {
        final String key = "key";
        final String value = "value";
        final String value1 = "value1";
        final String uri = "http://127.0.0.1:8080/abc";
        final HttpClient httpClient = HttpClient.ofDefault();
        final HttpRequestFacade httpRequestFacade = httpClient.post(uri)
                .addHeader(key, value)
                .addParam(key, value)
                .addParam(key, value1)
                .readTimeout(10)
                .maxRedirects(18)
                .maxRetries(18)
                .enableUriEncode();
        final SegmentRequest segmentRequest = httpRequestFacade.segment();

        assertEquals(httpRequestFacade.uri(), segmentRequest.uri());
        assertEquals(httpRequestFacade.headers().toString(), segmentRequest.headers().toString());
        assertEquals(httpRequestFacade.paramNames().toString(), segmentRequest.paramNames().toString());
        assertEquals(httpRequestFacade.getParams(key).toString(), segmentRequest.getParams(key).toString());
        assertEquals(httpRequestFacade.readTimeout(), segmentRequest.readTimeout());
        assertEquals(httpRequestFacade.uriEncode(), segmentRequest.uriEncode());

        assertEquals(((HttpRequestBaseImpl) httpRequestFacade).ctx.maxRetries(),
                ((HttpRequestBaseImpl) segmentRequest).ctx.maxRetries());
        assertEquals(((HttpRequestBaseImpl) httpRequestFacade).ctx.maxRedirects(),
                ((HttpRequestBaseImpl) segmentRequest).ctx.maxRedirects());
        assertEquals(((HttpRequestBaseImpl) httpRequestFacade).handle,
                ((HttpRequestBaseImpl) segmentRequest).handle);
        assertEquals(((HttpRequestBaseImpl) httpRequestFacade).handler,
                ((HttpRequestBaseImpl) segmentRequest).handler);

        then(segmentRequest.isMultipart()).isFalse();
        then(segmentRequest.isFile()).isFalse();
        then(segmentRequest.isSegmented()).isTrue();
    }
}

