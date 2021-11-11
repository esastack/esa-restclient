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
package io.esastack.restclient.codec;

import io.esastack.httpclient.core.MultipartBody;
import io.esastack.httpclient.core.MultipartBodyImpl;
import io.esastack.restclient.codec.impl.FormURLEncodedEncoder;
import org.junit.jupiter.api.Test;

class FormURLEncodedEncoderTest {

    @Test
    void testEncode() {
        FormURLEncodedEncoder encoder = new FormURLEncodedEncoder();
        then(encoder.encode(null, null, null).content())
                .isEqualTo(null);

        MultipartBody multipartBody = new MultipartBodyImpl();
        then(encoder.encode(null, null, multipartBody).content().multipartEncode())
                .isEqualTo(false);
    }

}
