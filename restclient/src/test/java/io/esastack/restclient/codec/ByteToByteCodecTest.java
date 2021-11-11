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

import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestRequestBase;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.codec.impl.ByteToByteCodec;
import io.esastack.restclient.codec.impl.DecodeChainImpl;
import io.esastack.restclient.codec.impl.EncodeChainImpl;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.CodecException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ByteToByteCodecTest {

    @Test
    void testEncode() throws Exception {
        ByteToByteCodec byteCodec = new ByteToByteCodec();
        byte[] bytes = "hello".getBytes();
        EncodeContext encodeContext = new EncodeChainImpl(
                mock(RestRequestBase.class),
                bytes,
                byte[].class,
                byte[].class,
                mock(List.class),
                mock(List.class)
        );
        then(byteCodec.encode(encodeContext).value()).isEqualTo(bytes);

        EncodeContext encodeContext1 = new EncodeChainImpl(
                mock(RestRequestBase.class),
                "content",
                String.class,
                String.class,
                mock(List.class),
                mock(List.class)
        );
        assertThrows(CodecException.class, () ->
                byteCodec.encode(encodeContext1));

    }

    @Test
    void testDecode() throws Exception {
        ByteToByteCodec byteCodec = new ByteToByteCodec();
        byte[] bytes = "hello".getBytes();
        DecodeContext decodeContext = new DecodeChainImpl(
                mock(RestRequestBase.class),
                mock(RestResponse.class),
                mock(RestClientOptions.class),
                byte[].class,
                byte[].class,
                ByteBufAllocator.DEFAULT.buffer().writeBytes(bytes)
        );
        then(byteCodec.decode(decodeContext))
                .isEqualTo(bytes);

        DecodeContext decodeContext1 = new DecodeChainImpl(
                mock(RestRequestBase.class),
                mock(RestResponse.class),
                mock(RestClientOptions.class),
                String.class,
                String.class,
                ByteBufAllocator.DEFAULT.buffer().writeBytes(bytes)
        );

        assertThrows(CodecException.class, () ->
                byteCodec.decode(decodeContext1));
    }

}
