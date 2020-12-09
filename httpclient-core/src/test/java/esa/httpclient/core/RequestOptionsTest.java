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

import esa.commons.collection.HashMultiValueMap;
import esa.commons.collection.MultiValueMap;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import esa.commons.netty.http.Http1HeadersImpl;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;

class RequestOptionsTest {

    @Test
    void testBasic() {
        final HttpMethod method = HttpMethod.POST;
        final HttpUri uri = new HttpUri("http://127.0.0.1:8080/abc");
        final int readTimeout = ThreadLocalRandom.current().nextInt();
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

        final RequestOptions options1 = new RequestOptions(method,
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
        then(options1.method()).isSameAs(method);
        then(options1.uri()).isNotSameAs(uri);
        then(options1.uri()).isEqualTo(uri);
        then(options1.readTimeout()).isEqualTo(readTimeout);
        then(options1.uriEncodeEnabled()).isEqualTo(uriEncodeEnabled);
        then(options1.maxRetries()).isEqualTo(maxRetries);
        then(options1.maxRedirects()).isEqualTo(maxRedirects);
        then(options1.headers()).isNotSameAs(headers);
        then(options1.expectContinueEnabled()).isEqualTo(expectContinueEnabled);
        then(options1.handle()).isSameAs(handle);
        then(options1.handler()).isSameAs(handler);

        then(options1.body()).isSameAs(body);
        then(options1.file()).isSameAs(file);

        then(options1.multipart()).isEqualTo(multipart);
        then(options1.attributes()).isNull();
        then(options1.files()).isNull();

        final MultiValueMap<String, String> attributes = new HashMultiValueMap<>();
        final List<MultipartFileItem> fileItems = new ArrayList<>();
        attributes.add("a", "a");
        final MultipartFileItem item = new MultipartFileItem("a", "", new File("/abc"),
                "xx", true);
        fileItems.add(item);

        final RequestOptions options2 = new RequestOptions(method,
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
                attributes,
                fileItems);

        attributes.add("xxx", "yyyy");
        fileItems.add(new MultipartFileItem("b", "xx", new File("/abcd"),
                "xxxx", false));

        then(options2.attributes().size()).isEqualTo(1);
        then(options2.attributes().getFirst("a")).isEqualTo("a");
        then(options2.attributes().getFirst("xxx")).isNull();

        then(options2.multipart()).isEqualTo(multipart);
        then(options2.files().size()).isEqualTo(1);
        then(options2.files().get(0)).isSameAs(item);
    }
}
