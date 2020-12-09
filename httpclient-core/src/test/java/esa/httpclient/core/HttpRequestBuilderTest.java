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
package esa.httpclient.core;

import esa.commons.collection.MultiValueMap;
import esa.commons.http.HttpHeaderValues;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.BDDAssertions.then;

class HttpRequestBuilderTest {

    @Test
    void testBasic() {
        final Map<String, String> params = new HashMap<>();
        params.put("x", "y");
        params.put("m", "n");

        final Map<CharSequence, CharSequence> headers = new HashMap<>();
        headers.put("Cookie", "xxxxxxxxxx");
        headers.put("Session", "xxxxxx===");

        final boolean uriEncodeEnabled = ThreadLocalRandom.current().nextBoolean();
        final boolean expectContinueEnabled = ThreadLocalRandom.current().nextBoolean();
        final int readTimeout = ThreadLocalRandom.current().nextInt(1, 3000);
        final int maxRetries = ThreadLocalRandom.current().nextInt();
        final int maxRedirects = ThreadLocalRandom.current().nextInt();

        final HttpRequestBuilder builder = HttpRequest.get("http://127.0.0.1:8080/abc")
                .addHeaders(headers)
                .addHeader("HOST", "127.0.0.1")
                .addParams(params)
                .addParam("a", "b")
                .setHeader("xx", "xxx")
                .uriEncodeEnabled(uriEncodeEnabled)
                .maxRetries(maxRetries)
                .maxRedirects(maxRedirects)
                .expectContinueEnabled(expectContinueEnabled)
                .readTimeout(readTimeout);

        final HttpRequest request = builder.build();
        builder.maxRetries(-1).maxRedirects(-1).expectContinueEnabled(null).readTimeout(1)
                .addHeader("xxxx", "yyyyyy")
                .addParam("mmmmm", "nnnn");

        then(request.method()).isEqualTo(HttpMethod.GET);
        then(((PlainRequest) request).body()).isNull();
        then(request.uri().toString()).isEqualTo("http://127.0.0.1:8080/abc");
        then(request.scheme()).isEqualTo(Scheme.HTTP.name0());
        then(request.config().uriEncodeEnabled()).isEqualTo(uriEncodeEnabled);
        then(request.config().maxRetries()).isEqualTo(maxRetries);
        then(request.config().maxRedirects()).isEqualTo(maxRedirects);
        then(request.config().expectContinueEnabled()).isEqualTo(expectContinueEnabled);
        then(request.config().readTimeout()).isEqualTo(readTimeout);

        final HttpHeaders headers0 = request.headers();
        then(headers0.size()).isEqualTo(4);
        then(headers0.get("Cookie")).isEqualTo("xxxxxxxxxx");
        then(headers0.get("Session")).isEqualTo("xxxxxx===");
        then(headers0.get("HOST")).isEqualTo("127.0.0.1");
        then(headers0.get("xx")).isEqualTo("xxx");
        then(headers0.get("xxxx")).isNull();

        final MultiValueMap<String, String> params0 = request.uri().params();
        then(params0.size()).isEqualTo(3);
        then(params0.getFirst("x")).isEqualTo("y");
        then(params0.getFirst("m")).isEqualTo("n");
        then(params0.getFirst("a")).isEqualTo("b");
        then(params0.getFirst("mmmmm")).isNull();
    }

    @Test
    void testBodyData() {
        final HttpRequestBuilder.BodyPermittedBuilder builder = HttpRequest.post("http://127.0.0.1:8080/abc");

        final byte[] data = "Hello World".getBytes();
        builder.body(data);
        then(builder.build()).isInstanceOf(PlainRequest.class);
        then(((PlainRequest) builder.build()).body()).isSameAs(data);

        final File file = new File("/abc");
        builder.file(file);
        then(builder.build()).isInstanceOf(FileRequest.class);
        then(((FileRequest) builder.build()).file()).isSameAs(file);
    }

    @Test
    void testMultipart() {
        final HttpRequestBuilder.Multipart builder = HttpRequest.multipart("http://127.0.0.1:8080/abc");
        builder.attribute("a", "b");
        builder.attribute("x", "y");

        final File file = new File("/abc");
        builder.file("a1", file);
        builder.file("a2", file, HttpHeaderValues.APPLICATION_OCTET_STREAM);
        builder.file("a3", file, "xxx", true);
        builder.file("a4", "file1", file, HttpHeaderValues.TEXT_PLAIN, true);

        final MultipartRequest request = builder.build();
        builder.attribute("xxxxx", "mmmmmmm");
        builder.file("xxxx", new File("/def"), "mmm", true);

        then(request.attributes().size()).isEqualTo(2);
        then(request.attributes().getFirst("a")).isEqualTo("b");
        then(request.attributes().getFirst("x")).isEqualTo("y");
        then(request.attributes().getFirst("xxxxx")).isNull();

        then(request.files().size()).isEqualTo(4);
        MultipartFileItem item;
        String name;
        for (int i = 0; i < request.files().size(); i++) {
            item = request.files().get(i);
            name = item.name();
            switch (name) {
                case "a1":
                    then(item.name()).isEqualTo("a1");
                    then(item.fileName()).isEqualTo(file.getName());
                    then(item.file()).isSameAs(file);
                    then(item.contentType()).isEqualTo(HttpHeaderValues.APPLICATION_OCTET_STREAM);
                    then(item.isText()).isFalse();
                    break;
                case "a2":
                    then(item.name()).isEqualTo("a2");
                    then(item.fileName()).isEqualTo(file.getName());
                    then(item.file()).isSameAs(file);
                    then(item.contentType()).isEqualTo(HttpHeaderValues.APPLICATION_OCTET_STREAM);
                    then(item.isText()).isFalse();
                    break;
                case "a3":
                    then(item.name()).isEqualTo("a3");
                    then(item.fileName()).isEqualTo(file.getName());
                    then(item.file()).isSameAs(file);
                    then(item.contentType()).isEqualTo("xxx");
                    then(item.isText()).isTrue();
                    break;
                default:
                    then(item.name()).isEqualTo("a4");
                    then(item.fileName()).isEqualTo("file1");
                    then(item.file()).isSameAs(file);
                    then(item.contentType()).isEqualTo(HttpHeaderValues.TEXT_PLAIN);
                    then(item.isText()).isTrue();
                    break;
            }
        }
    }

}
