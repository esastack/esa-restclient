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
import io.esastack.restclient.codec.impl.FastJsonCodec;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.CodecException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FastJsonCodecTest {

    @Test
    void testEncode() throws Exception {
        FastJsonCodec fastJsonCodec = new FastJsonCodec();
        RestRequestBase request = mock(RestRequestBase.class);
        when(request.contentType()).thenReturn(MediaTypeUtil.TEXT_PLAIN);

        Person person = new Person("Bob", "boy");
        EncodeContext ctx = new EncodeChainImpl(
                request,
                person,
                Person.class,
                Person.class,
                mock(List.class),
                mock(List.class)
        );
        assertThrows(CodecException.class, () ->
                fastJsonCodec.encode(ctx));

        when(ctx.contentType()).thenReturn(MediaTypeUtil.APPLICATION_JSON_UTF8);
        then(fastJsonCodec.encode(ctx).value())
                .isEqualTo(JSON.toJSONBytes(person));
    }

    @Test
    void testDecode() throws Exception {
        FastJsonCodec fastJsonCodec = new FastJsonCodec();
        Person person = new Person("Bob", "boy");

        RestResponse response = mock(RestResponse.class);
        when(response.contentType()).thenReturn(MediaTypeUtil.TEXT_PLAIN);
        DecodeContext ctx = new DecodeChainImpl(
                mock(RestRequestBase.class),
                response,
                mock(RestClientOptions.class),
                Person.class,
                Person.class,
                ByteBufAllocator.DEFAULT.buffer().writeBytes(JSON.toJSONBytes(person))
        );
        assertThrows(CodecException.class, () ->
                fastJsonCodec.decode(ctx));

        when(response.contentType()).thenReturn(MediaTypeUtil.APPLICATION_JSON_UTF8);
        then(fastJsonCodec.decode(ctx))
                .isEqualTo(person);
    }

}
