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

import io.esastack.restclient.ResponseBodyContent;
import io.esastack.restclient.codec.impl.ByteToByteCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ByteToByteCodecTest {

    @Test
    void testEncode() throws Exception {
        ByteToByteCodec byteCodec = new ByteToByteCodec();
        then(byteCodec.encode(null, null, null).content()).isEqualTo(null);

        assertThrows(ClassCastException.class, () ->
                byteCodec.encode(null, null, "Hello"));

        byte[] bytes = "Hello".getBytes();
        then(byteCodec.encode(null, null, bytes).content()).isEqualTo(bytes);
    }

    @Test
    void testDecode() throws Exception {
        ByteToByteCodec byteCodec = new ByteToByteCodec();
        then((Object) byteCodec.decode(null, null, ResponseBodyContent.of(null), byte[].class))
                .isEqualTo(null);

        byte[] bytes = "Hello".getBytes();
        then((Object) byteCodec.decode(null, null, ResponseBodyContent.of(bytes), byte[].class))
                .isEqualTo(bytes);
    }

}
