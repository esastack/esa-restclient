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
import esa.httpclient.core.netty.NettyHttpClient;
import esa.httpclient.core.util.Futures;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Java6BDDAssertions.then;
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
        final ChunkRequest chunk0 = mock(ChunkRequest.class);

        final CompositeRequest request = new CompositeRequest(builder,
                client, () -> chunk0, method, uri);

        final MultipartRequest multipart = request.multipart();
        then(multipart.isMultipart()).isTrue();
        then(multipart.isSegmented()).isFalse();
        then(multipart.bytes()).isNull();
        then(multipart.file()).isNull();
        then(multipart.attributes()).isEmpty();
        then(multipart.files()).isEmpty();

        multipart.attribute("a", "b");
        multipart.attribute("x", "y");
        multipart.attribute("xxxxx", "mmmmmmm");

        final File file = new File("/abc");
        multipart.file("a1", file);
        multipart.file("a2", file, HttpHeaderValues.APPLICATION_OCTET_STREAM);
        multipart.file("a3", file, "xxx", true);
        multipart.file("a4", "file1", file, HttpHeaderValues.TEXT_PLAIN, true);
        multipart.file("xxxx", new File("/def"), "mmm", true);
        then(multipart.multipartEncode()).isTrue();
        multipart.multipartEncode(false);
        then(multipart.multipartEncode()).isFalse();

        then(request.attributes().size()).isEqualTo(3);
        then(request.files().size()).isEqualTo(5);

        then(request.segment()).isSameAs(chunk0);
        then(request.body(new File("")).file()).isNotNull();
        then(request.body("Hello".getBytes())).isNotNull();
    }

    @Test
    void testCopy() {
        final String uri = "http://127.0.0.1:8080/abc";
        final HttpClientBuilder builder = HttpClient.create();
        final NettyHttpClient client = mock(NettyHttpClient.class);
        final HttpMethod method = HttpMethod.PUT;
        final ChunkRequest chunk0 = mock(ChunkRequest.class);
        final boolean multipartEncode = true;
        final byte[] data = "Hello".getBytes();

        final CompositeRequest request = new CompositeRequest(builder,
                client, () -> chunk0, method, uri);

        final MultipartRequest multipart = request.multipart();
        multipart.attribute("a", "b");
        multipart.attribute("x", "y");
        multipart.attribute("xxxxx", "mmmmmmm");

        final File file = new File("/abc");
        multipart.file("a1", file);
        multipart.file("a2", file, HttpHeaderValues.APPLICATION_OCTET_STREAM);
        multipart.file("a3", file, "xxx", true);
        multipart.file("a4", "file1", file, HttpHeaderValues.TEXT_PLAIN, true);
        multipart.file("xxxx", new File("/def"), "mmm", true);
        multipart.multipartEncode(multipartEncode);

        then(request.attributes().size()).isEqualTo(3);
        then(request.files().size()).isEqualTo(5);

        request.body(file);
        request.body(data);

        final CompositeRequest copied = request.copy();
        then(copied.multipartEncode()).isEqualTo(multipartEncode);
        then(copied.attributes().size()).isEqualTo(3);
        then(copied.files().size()).isEqualTo(5);
        then(copied.file()).isNull();
        then(Arrays.equals(request.bytes(), data)).isTrue();

        // Test isolation
        request.file("m", file);
        request.attribute("mm", "nn");
        then(request.attributes().size()).isEqualTo(4);
        then(request.files().size()).isEqualTo(6);
        then(copied.attributes().size()).isEqualTo(3);
        then(copied.files().size()).isEqualTo(5);
    }

    @Test
    void testUnmodifiableAfterStarted() {
        final String uri = "http://127.0.0.1:8080/abc";
        final HttpClientBuilder builder = HttpClient.create();
        final NettyHttpClient client = mock(NettyHttpClient.class);
        final HttpMethod method = HttpMethod.PUT;
        final ChunkRequest chunk0 = mock(ChunkRequest.class);
        final byte[] data = "Hello".getBytes();

        final CompositeRequest request = new CompositeRequest(builder,
                client, () -> chunk0, method, uri);
        when(client.execute(any(), any(), any(), any()))
                .thenReturn(Futures.completed(mock(HttpResponse.class)));

        // Before writing
        request.expectContinueEnabled(true);

        request.uriEncodeEnabled(true);
        then(request.uriEncodeEnabled()).isTrue();

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
        request.attribute("a", "b");
        request.multipartEncode(true);
        request.body(file);
        request.body(data);

        request.execute();

        // After writing
        assertThrows(IllegalStateException.class, () -> request.expectContinueEnabled(true));
        assertThrows(IllegalStateException.class, () -> request.uriEncodeEnabled(true));
        assertThrows(IllegalStateException.class, () -> request.maxRedirects(10));
        assertThrows(IllegalStateException.class, () -> request.maxRetries(10));
        assertThrows(IllegalStateException.class, () -> request.readTimeout(100));
        assertThrows(IllegalStateException.class, () -> request.addHeaders(Collections.singletonMap("a", "b")));
        assertThrows(IllegalStateException.class, () -> request.addParams(Collections.singletonMap("m", "n")));
        assertThrows(IllegalStateException.class, () -> request.handle((h) -> {
        }));
        assertThrows(IllegalStateException.class, () -> request.handler(mock(Handler.class)));
        assertThrows(IllegalStateException.class, () -> request.addHeader("x", "y"));
        assertThrows(IllegalStateException.class, () -> request.setHeader("a", "bb"));
        assertThrows(IllegalStateException.class, () -> request.removeHeader("a"));
        assertThrows(IllegalStateException.class, () -> request.addParam("p", "q"));

        assertThrows(IllegalStateException.class, () -> request.file("a1", file));
        assertThrows(IllegalStateException.class,
                () -> request.file("a2", file, HttpHeaderValues.APPLICATION_OCTET_STREAM));
        assertThrows(IllegalStateException.class,
                () -> request.file("a3", file, "xxx", true));
        assertThrows(IllegalStateException.class,
                () -> request.file("a4", "file1", file, HttpHeaderValues.TEXT_PLAIN, true));
        assertThrows(IllegalStateException.class, () -> request.attribute("a", "b"));
        assertThrows(IllegalStateException.class, () -> request.multipartEncode(true));
        assertThrows(IllegalStateException.class, () -> request.body(file));
        assertThrows(IllegalStateException.class, () -> request.body(data));

        assertThrows(IllegalStateException.class, request::execute);
    }

}

