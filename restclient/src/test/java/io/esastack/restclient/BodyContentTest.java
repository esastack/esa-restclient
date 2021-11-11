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
import org.junit.jupiter.api.Test;

import java.io.File;

class BodyContentTest {

    @Test
    void testRequestBodyContent() {
        BodyContent<File> fileContent = RequestBodyContent.of((File) null);
        then(fileContent.content()).isEqualTo(null);
        File file = new File("test");
        fileContent = RequestBodyContent.of(file);
        then(fileContent.content()).isEqualTo(file);

        BodyContent<byte[]> bytesContent = RequestBodyContent.of((byte[]) null);
        then(bytesContent.content()).isEqualTo(null);
        byte[] bytes = "test".getBytes();
        bytesContent = RequestBodyContent.of(bytes);
        then(bytesContent.content()).isEqualTo(bytes);

        BodyContent<MultipartBody> multipartContent = RequestBodyContent.of((MultipartBody) null);
        then(multipartContent.content()).isEqualTo(null);
        MultipartBody multipart = new MultipartBodyImpl();
        multipartContent = RequestBodyContent.of(multipart);
        then(multipartContent.content()).isEqualTo(multipart);
    }

    @Test
    void testResponseBodyContent() {
        BodyContent<byte[]> bytesContent = ResponseBodyContent.of(null);
        then(bytesContent.content()).isEqualTo(null);
        byte[] bytes = "test".getBytes();
        bytesContent = ResponseBodyContent.of(bytes);
        then(bytesContent.content()).isEqualTo(bytes);
    }
}
