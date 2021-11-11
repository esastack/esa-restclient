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

import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.ResponseBodyContent;
import io.esastack.restclient.codec.impl.StringCodec;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.BDDAssertions.then;

class StringCodecTest {

    @Test
    void testEncode() throws Exception {

        StringCodec codec = new StringCodec();
        then(codec.encode(MediaTypeUtil.of("text", "plain", StandardCharsets.UTF_16),
                null, null).content()).isEqualTo(null);

        String content = "content";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_16);
        then(codec.encode(MediaTypeUtil.of("text", "plain", StandardCharsets.UTF_16),
                null, content).content()).isEqualTo(bytes);

        bytes = content.getBytes(StandardCharsets.UTF_8);
        then(codec.encode(MediaTypeUtil.of("text", "plain"),
                null, content).content()).isEqualTo(bytes);

    }

    @Test
    void testDecode() throws Exception {
        StringCodec codec = new StringCodec();
        then((String) codec.decode(MediaTypeUtil.of("text", "plain", StandardCharsets.UTF_16),
                null, ResponseBodyContent.of(null), String.class)).isEqualTo(null);

        String content = "content";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_16);
        then((String) codec.decode(MediaTypeUtil.of("text", "plain", StandardCharsets.UTF_16),
                null, ResponseBodyContent.of(bytes), String.class)).isEqualTo(content);

        bytes = content.getBytes(StandardCharsets.UTF_8);
        then((String) codec.decode(MediaTypeUtil.of("text", "plain"),
                null, ResponseBodyContent.of(bytes), String.class)).isEqualTo(content);
    }
}
