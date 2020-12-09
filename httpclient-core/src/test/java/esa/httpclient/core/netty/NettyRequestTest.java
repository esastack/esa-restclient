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

import esa.commons.collection.HashMultiValueMap;
import esa.commons.collection.MultiValueMap;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.httpclient.core.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class NettyRequestTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> NettyRequest.from(null));

        final HttpMethod method = HttpMethod.CONNECT;
        final HttpUri uri = new HttpUri("http://127.0.0.1:8080/abc/s");
        final int readTimeout = ThreadLocalRandom.current().nextInt(5000);
        final boolean uriEncodeEnabled = ThreadLocalRandom.current().nextBoolean();
        final HttpHeaders headers = new Http1HeadersImpl();
        final boolean expectContinueEnabled = ThreadLocalRandom.current().nextBoolean();
        final byte[] body = "Hello World".getBytes();
        final File file = new File("/abc");
        final int maxRetries = ThreadLocalRandom.current().nextInt();
        final int maxRedirects = ThreadLocalRandom.current().nextInt();
        final Handler handler = mock(Handler.class);
        final Consumer<Handle> handle = (h) -> {};

        final PlainRequest request1 = NettyRequest.from(method,
                uri,
                readTimeout,
                uriEncodeEnabled,
                expectContinueEnabled,
                maxRetries,
                maxRedirects,
                headers,
                handle,
                handler,
                body);

        then(request1.type()).isEqualTo(RequestType.PLAIN);
        then(request1.body()).isSameAs(body);

        final FileRequest request2 = NettyRequest.from(method,
                uri,
                readTimeout,
                uriEncodeEnabled,
                expectContinueEnabled,
                maxRetries,
                maxRedirects,
                headers,
                handle,
                handler,
                file);
        then(request2.type()).isEqualTo(RequestType.FILE);
        then(request2.file()).isSameAs(file);

        final MultiValueMap<String, String> attributes = new HashMultiValueMap<>();
        final List<MultipartFileItem> files = new ArrayList<>();
        attributes.add("a", "a");
        final MultipartFileItem item = new MultipartFileItem("a", "", new File("/abc"),
                "xx", true);
        files.add(item);
        final MultipartRequest request3 = NettyRequest.from(method,
                uri,
                readTimeout,
                uriEncodeEnabled,
                expectContinueEnabled,
                maxRetries,
                maxRedirects,
                headers,
                handle,
                handler,
                true,
                attributes,
                files);
        then(request3.type()).isEqualTo(RequestType.MULTIPART);
        then(request3.files()).isEqualTo(files);
        then(request3.attributes()).isEqualTo(attributes);

        assertThrows(IllegalStateException.class, () -> {
            NettyRequest.from(method,
                    uri,
                    readTimeout,
                    uriEncodeEnabled,
                    expectContinueEnabled,
                    maxRetries,
                    maxRedirects,
                    headers,
                    handle,
                    handler,
                    true,
                    new HashMultiValueMap<>(),
                    Collections.emptyList());
        });
    }

    @Test
    void testCommon() {
        final HttpMethod method = HttpMethod.CONNECT;
        final HttpUri uri = new HttpUri("http://127.0.0.1:8080/abc/s");
        final int readTimeout = ThreadLocalRandom.current().nextInt(5000);
        final boolean uriEncodeEnabled = ThreadLocalRandom.current().nextBoolean();
        final HttpHeaders headers = new Http1HeadersImpl();
        final boolean expectContinueEnabled = ThreadLocalRandom.current().nextBoolean();
        final byte[] body = "Hello World".getBytes();
        final File file = new File("/abc");
        final int maxRetries = ThreadLocalRandom.current().nextInt();
        final int maxRedirects = ThreadLocalRandom.current().nextInt();
        final boolean multipart = ThreadLocalRandom.current().nextBoolean();
        final Handler handler = mock(Handler.class);
        final Consumer<Handle> handle = (h) -> {};

        final RequestOptions options = new RequestOptions(method,
                uri,
                readTimeout,
                uriEncodeEnabled,
                maxRetries,
                maxRedirects,
                headers,
                expectContinueEnabled,
                handle,
                handler,
                body,
                file,
                multipart,
                null,
                null);

        HttpRequest request = NettyRequest.from(options);
        then(request.method()).isEqualTo(method);
        then(request.uri()).isEqualTo(uri);
        then(request.scheme()).isEqualTo(Scheme.HTTP.name0());
        then(request.path()).isEqualTo(uri.netURI().getPath());
        then(request.type()).isEqualTo(RequestType.FILE);

        request.addParam("a", "b");
        request.addParam("a", "c");
        then(request.getParam("a")).isEqualTo("b");
        then(request.getParams("a").size()).isEqualTo(2);

        final List<String> values = request.getParams("a");
        then(values.contains("b")).isTrue();
        then(values.contains("c")).isTrue();

        request.addParam("a", "d");
        then(request.getParams("a").size()).isEqualTo(3);
        then(values.contains("b")).isTrue();
        then(values.contains("c")).isTrue();

        request.addHeader("a", "b");
        request.addHeader("a", "c");
        then(request.getHeader("a")).isEqualTo("b");
        then(request.headers().getAll("a").contains("b")).isTrue();
        then(request.headers().getAll("a").contains("c")).isTrue();

        request.setHeader("a", "d");
        then(request.headers().getAll("a").size()).isEqualTo(1);
        then(request.headers().get("a")).isEqualTo("d");
    }
}
