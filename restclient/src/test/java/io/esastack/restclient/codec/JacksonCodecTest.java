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
import io.esastack.restclient.codec.impl.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.BDDAssertions.then;

public class JacksonCodecTest {
    @Test
    void testEncode() throws Exception {
        JacksonCodec jacksonCodec = new JacksonCodec();
        then(jacksonCodec.encode(MediaTypeUtil.APPLICATION_JSON_UTF8, null, null).content())
                .isEqualTo("null".getBytes(StandardCharsets.UTF_8));

        Person person = new Person("LiMing", "boy");
        then(jacksonCodec.encode(null, null, person).content())
                .isEqualTo(JacksonCodec.getDefaultMapper().writeValueAsBytes(person));
    }

    @Test
    void testDecode() throws Exception {
        JacksonCodec jacksonCodec = new JacksonCodec();
        then((Object) jacksonCodec.decode(null, null, ResponseBodyContent.of(null), null))
                .isEqualTo(null);

        Person person = new Person("LiMing", "boy");
        byte[] bytes = JacksonCodec.getDefaultMapper().writeValueAsBytes(person);
        then((Object) jacksonCodec.decode(null, null, ResponseBodyContent.of(bytes), Person.class))
                .isEqualTo(person);
    }
}
