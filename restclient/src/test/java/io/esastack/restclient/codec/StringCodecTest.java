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
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestRequestBase;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.codec.impl.DecodeChainImpl;
import io.esastack.restclient.codec.impl.EncodeChainImpl;
import io.esastack.restclient.codec.impl.StringCodec;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.CodecException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StringCodecTest {

    @Test
    void testEncode() throws Exception {

        StringCodec codec = new StringCodec();

        RestRequestBase request = mock(RestRequestBase.class);
        when(request.contentType()).thenReturn(MediaTypeUtil.APPLICATION_JSON);
        String data = "data";

        EncodeContext encodeContext = new EncodeChainImpl(
                request,
                data,
                Person.class,
                Person.class,
                mock(List.class),
                mock(List.class)
        );
        assertThrows(CodecException.class, () ->
                codec.encode(encodeContext));

        EncodeContext encodeContext1 = new EncodeChainImpl(
                request,
                data,
                String.class,
                String.class,
                mock(List.class),
                mock(List.class)
        );
        when(request.contentType()).thenReturn(MediaTypeUtil.TEXT_PLAIN);
        then(codec.encode(encodeContext1).value())
                .isEqualTo(data.getBytes(StandardCharsets.UTF_8));

        EncodeContext encodeContext2 = new EncodeChainImpl(
                request,
                data,
                String.class,
                String.class,
                mock(List.class),
                mock(List.class)
        );
        when(request.contentType()).thenReturn(MediaTypeUtil.of("text", "plain", StandardCharsets.UTF_16));
        then(codec.encode(encodeContext2).value())
                .isEqualTo(data.getBytes(StandardCharsets.UTF_16));
    }

    @Test
    void testDecode() throws Exception {
        StringCodec codec = new StringCodec();

        String data = "data";

        RestResponse response = mock(RestResponse.class);
        when(response.contentType()).thenReturn(MediaTypeUtil.TEXT_PLAIN);
        DecodeContext decodeContext = new DecodeChainImpl(
                mock(RestRequestBase.class),
                response,
                mock(RestClientOptions.class),
                Person.class,
                Person.class,
                ByteBufAllocator.DEFAULT.buffer().writeBytes(data.getBytes())
        );
        assertThrows(CodecException.class, () -> codec.decode(decodeContext));

        DecodeContext decodeContext1 = new DecodeChainImpl(
                mock(RestRequestBase.class),
                response,
                mock(RestClientOptions.class),
                String.class,
                String.class,
                ByteBufAllocator.DEFAULT.buffer().writeBytes(data.getBytes(StandardCharsets.UTF_8))
        );
        when(response.contentType()).thenReturn(
                MediaTypeUtil.of("text", "plain", StandardCharsets.UTF_8));
        then(codec.decode(decodeContext1)).isEqualTo(data);

        DecodeContext decodeContext2 = new DecodeChainImpl(
                mock(RestRequestBase.class),
                response,
                mock(RestClientOptions.class),
                String.class,
                String.class,
                ByteBufAllocator.DEFAULT.buffer().writeBytes(data.getBytes(StandardCharsets.UTF_16))
        );
        when(response.contentType()).thenReturn(
                MediaTypeUtil.of("text", "plain", StandardCharsets.UTF_16));
        then(codec.decode(decodeContext2)).isEqualTo(data);
    }
}
