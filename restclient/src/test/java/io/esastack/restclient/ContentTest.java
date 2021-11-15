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
package io.esastack.restclient;

import io.esastack.httpclient.core.MultipartBody;
import io.esastack.httpclient.core.MultipartBodyImpl;
import io.esastack.restclient.codec.RequestContent;
import io.esastack.restclient.codec.ResponseContent;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;


class ContentTest {

    @Test
    void testRequestContent() {
        assertThrows(NullPointerException.class, () -> RequestContent.of((File) null));
        File file = new File("test");
        RequestContent<File> fileContent = RequestContent.of(file);
        then(fileContent.value()).isEqualTo(file);

        assertThrows(NullPointerException.class, () -> RequestContent.of((byte[]) null));
        byte[] bytes = "test".getBytes();
        RequestContent<byte[]> bytesContent = RequestContent.of(bytes);
        then(bytesContent.value()).isEqualTo(bytes);

        assertThrows(NullPointerException.class, () -> RequestContent.of((MultipartBody) null));
        MultipartBody multipart = new MultipartBodyImpl();
        RequestContent<MultipartBody> multipartContent = RequestContent.of(multipart);
        then(multipartContent.value()).isEqualTo(multipart);
    }

    @Test
    void testResponseBodyContent() {
        assertThrows(NullPointerException.class, () -> ResponseContent.of(null));
        byte[] bytes = "test".getBytes();
        ResponseContent<byte[]> bytesContent = ResponseContent.of(bytes);
        then(bytesContent.value()).isEqualTo(bytes);
    }
}
