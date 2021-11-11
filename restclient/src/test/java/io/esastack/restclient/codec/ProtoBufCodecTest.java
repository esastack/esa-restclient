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

import com.alibaba.fastjson.JSON;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestRequestBase;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.codec.impl.DecodeChainImpl;
import io.esastack.restclient.codec.impl.EncodeChainImpl;
import io.esastack.restclient.codec.impl.ProtoBufCodec;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.CodecException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProtoBufCodecTest {

    @Test
    void testEncode() {
        ProtoBufCodec protoBufCodec = new ProtoBufCodec();

        RestRequestBase request = mock(RestRequestBase.class);
        when(request.contentType()).thenReturn(MediaTypeUtil.TEXT_PLAIN);

        EncodeContext encodeContext = new EncodeChainImpl(
                request,
                "aaa",
                String.class,
                String.class,
                mock(List.class),
                mock(List.class)
        );
        assertThrows(CodecException.class, () ->
                protoBufCodec.encode(encodeContext));
    }

    @Test
    void testDecode() {
        ProtoBufCodec protoBufCodec = new ProtoBufCodec();

        RestResponse response = mock(RestResponse.class);
        when(response.contentType()).thenReturn(MediaTypeUtil.TEXT_PLAIN);
        DecodeContext decodeContext = new DecodeChainImpl(
                mock(RestRequestBase.class),
                response,
                mock(RestClientOptions.class),
                String.class,
                String.class,
                ByteBufAllocator.DEFAULT.buffer().writeBytes(JSON.toJSONBytes("aaa"))
        );
        assertThrows(CodecException.class, () ->
                protoBufCodec.decode(decodeContext));
    }
}
