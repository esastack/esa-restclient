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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestRequestBase;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.codec.impl.DecodeChainImpl;
import io.esastack.restclient.codec.impl.EncodeChainImpl;
import io.esastack.restclient.codec.impl.GsonCodec;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.CodecException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GsonCodecTest {

    private final Gson gson = new GsonBuilder().create();

    @Test
    void testEncode() throws Exception {
        GsonCodec gsonCodec = new GsonCodec();
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
                gsonCodec.encode(ctx));

        when(request.contentType()).thenReturn(MediaTypeUtil.APPLICATION_JSON);
        then(gsonCodec.encode(ctx).value())
                .isEqualTo(gson.toJson(person).getBytes(StandardCharsets.UTF_8));

        when(request.contentType()).thenReturn(MediaTypeUtil.of("application", "json", StandardCharsets.UTF_16));
        then(gsonCodec.encode(ctx).value())
                .isEqualTo(gson.toJson(person).getBytes(StandardCharsets.UTF_16));
    }

    @Test
    void testDecode() throws Exception {
        GsonCodec gsonCodec = new GsonCodec();
        Person person = new Person("Bob", "boy");

        RestResponse response = mock(RestResponse.class);
        when(response.contentType()).thenReturn(MediaTypeUtil.TEXT_PLAIN);
        DecodeContext ctx = new DecodeChainImpl(
                mock(RestRequestBase.class),
                response,
                mock(RestClientOptions.class),
                Person.class,
                Person.class,
                ByteBufAllocator.DEFAULT.buffer().writeBytes(gson.toJson(person).getBytes(StandardCharsets.UTF_8))
        );

        assertThrows(CodecException.class, () ->
                gsonCodec.decode(ctx));

        when(response.contentType()).thenReturn(MediaTypeUtil.APPLICATION_JSON);
        then(gsonCodec.decode(ctx))
                .isEqualTo(person);

        DecodeContext ctx1 = new DecodeChainImpl(
                mock(RestRequestBase.class),
                response,
                mock(RestClientOptions.class),
                Person.class,
                Person.class,
                ByteBufAllocator.DEFAULT.buffer().writeBytes(
                        gson.toJson(person).getBytes(StandardCharsets.UTF_16))
        );
        when(response.contentType()).thenReturn(
                MediaTypeUtil.of("application", "json", StandardCharsets.UTF_16));

        then(gsonCodec.decode(ctx1)).isEqualTo(person);
    }
}
