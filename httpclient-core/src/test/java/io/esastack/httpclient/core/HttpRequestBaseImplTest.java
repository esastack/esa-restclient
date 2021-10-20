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
package io.esastack.httpclient.core;

import esa.commons.collection.MultiValueMap;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;

class HttpRequestBaseImplTest {

    @Test
    void testBasic() {
        final HttpClientBuilder builder = HttpClient.create();

        final Map<String, String> params = new HashMap<>();
        params.put("x", "y");
        params.put("m", "n");

        final Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "xxxxxxxxxx");
        headers.put("Session", "xxxxxx===");

        final int readTimeout = ThreadLocalRandom.current().nextInt(1, 3000);
        final int maxRetries = ThreadLocalRandom.current().nextInt();
        final int maxRedirects = ThreadLocalRandom.current().nextInt();
        final Consumer<Handle> handle = (h) -> {
        };
        final Handler handler = mock(Handler.class);

        final HttpRequestBaseImpl request = new HttpRequestBaseImpl(builder,
                HttpMethod.PUT, "http://127.0.0.1:8080/abc");
        then(request.ctx.isUseExpectContinue()).isEqualTo(builder.isUseExpectContinue());
        then(request.ctx.maxRetries()).isEqualTo(builder.retryOptions().maxRetries());
        then(request.ctx.maxRedirects()).isEqualTo(builder.maxRedirects());
        then(request.readTimeout()).isEqualTo(builder.readTimeout());
        then(request.uriEncode()).isFalse();

        request.disableExpectContinue()
                .enableUriEncode()
                .maxRedirects(maxRedirects)
                .maxRetries(maxRetries)
                .readTimeout(readTimeout)
                .addHeaders(headers)
                .addHeader("HOST", "127.0.0.1")
                .addParams(params)
                .addParam("a", "b")
                .handle(handle)
                .handler(handler)
                .addHeader("xxxx", "yyyyyy")
                .addParam("mmmmm", "nnnn")
                .setHeader("xx", "xxx");

        then(request.method()).isEqualTo(HttpMethod.PUT);
        then(request.buffer()).isNull();
        then(request.file()).isNull();
        then(request.files()).isNull();
        then(request.attrs()).isNull();
        then(request.uri().toString()).isEqualTo("http://127.0.0.1:8080/abc");
        then(request.scheme()).isEqualTo(Scheme.HTTP.name0());

        then(request.uriEncode()).isTrue();
        then(request.readTimeout()).isEqualTo(readTimeout);

        then(request.ctx.maxRetries()).isEqualTo(maxRetries);
        then(request.ctx.maxRedirects()).isEqualTo(maxRedirects);
        then(request.ctx.isUseExpectContinue()).isFalse();

        final HttpHeaders headers0 = request.headers();
        then(headers0.size()).isEqualTo(5);
        then(headers0.get("Cookie")).isEqualTo("xxxxxxxxxx");
        then(headers0.get("Session")).isEqualTo("xxxxxx===");
        then(headers0.get("HOST")).isEqualTo("127.0.0.1");
        then(headers0.get("xx")).isEqualTo("xxx");
        then(headers0.get("xxxx")).isEqualTo("yyyyyy");

        final MultiValueMap<String, String> params0 = request.uri().params();
        then(params0.size()).isEqualTo(4);
        then(params0.getFirst("x")).isEqualTo("y");
        then(params0.getFirst("m")).isEqualTo("n");
        then(params0.getFirst("a")).isEqualTo("b");
        then(params0.getFirst("mmmmm")).isEqualTo("nnnn");
    }

    @Test
    void testCopy() {
        final Map<String, String> params = new HashMap<>();
        params.put("x", "y");
        params.put("m", "n");

        final Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "xxxxxxxxxx");
        headers.put("Session", "xxxxxx===");

        final HttpMethod method = HttpMethod.POST;
        final HttpUri uri = new HttpUri("http://127.0.0.1:8080/abc");
        final int readTimeout = ThreadLocalRandom.current().nextInt();
        final int maxRetries = ThreadLocalRandom.current().nextInt();
        final int maxRedirects = ThreadLocalRandom.current().nextInt();
        final Handler handler = mock(Handler.class);
        final Consumer<Handle> handle = (h) -> {
        };
        final HttpClientBuilder builder = HttpClient.create();

        final HttpRequestBase request1 = new HttpRequestBaseImpl(builder, method, uri.toString())
                .maxRetries(maxRetries)
                .maxRedirects(maxRedirects)
                .readTimeout(readTimeout)
                .addHeaders(headers)
                .addParams(params)
                .handle(handle)
                .handler(handler);

        final boolean useUriEncode = ThreadLocalRandom.current().nextBoolean();
        final boolean useExpectContinue = ThreadLocalRandom.current().nextBoolean();
        if (useUriEncode) {
            request1.enableUriEncode();
        }
        if (!useExpectContinue) {
            request1.disableExpectContinue();
        }

        // Test isolation
        final HttpRequestBaseImpl copied = (HttpRequestBaseImpl) request1.copy();

        request1.maxRetries(maxRetries - 1)
                .maxRedirects(maxRedirects - 1)
                .readTimeout(readTimeout - 1)
                .handler(null)
                .handle(null);

        then(copied.method()).isSameAs(method);
        then(copied.uri()).isNotSameAs(uri);
        then(copied.uri().toString()).isEqualTo(uri.toString());
        then(copied.readTimeout()).isEqualTo(readTimeout);
        then(copied.uriEncode()).isEqualTo(useUriEncode);
        then(copied.ctx.maxRetries()).isEqualTo(maxRetries);
        then(copied.ctx.maxRedirects()).isEqualTo(maxRedirects);
        then(copied.headers().size()).isEqualTo(2);
        then(copied.paramNames().size()).isEqualTo(2);

        if (useExpectContinue) {
            // default to HttpClientBuilder#isEnableExpectContinue()
            then(copied.ctx.isUseExpectContinue()).isEqualTo(builder.isUseExpectContinue());
        } else {
            then(copied.ctx.isUseExpectContinue()).isFalse();
        }
        then(copied.handle).isNull();
        then(copied.handler).isNotNull();

        request1.headers().clear();
        request1.addParams(headers);
        then(copied.headers().size()).isEqualTo(2);
        then(copied.paramNames().size()).isEqualTo(2);
    }

}

