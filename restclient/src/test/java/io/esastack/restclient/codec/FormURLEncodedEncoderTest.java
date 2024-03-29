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

import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.MultipartBody;
import io.esastack.httpclient.core.MultipartBodyImpl;
import io.esastack.restclient.RestRequestBase;
import io.esastack.restclient.codec.impl.EncodeChainImpl;
import io.esastack.restclient.codec.impl.FormURLEncodedEncoder;
import io.netty.handler.codec.CodecException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormURLEncodedEncoderTest {

    @Test
    void testEncode() throws Exception {
        FormURLEncodedEncoder encoder = new FormURLEncodedEncoder();
        RestRequestBase request = mock(RestRequestBase.class);
        when(request.contentType()).thenReturn(MediaType.TEXT_PLAIN);

        MultipartBody multipartBody = new MultipartBodyImpl();
        EncodeContext ctx = new EncodeChainImpl(
                request,
                multipartBody,
                MultipartBody.class,
                MultipartBody.class,
                mock(List.class),
                mock(List.class)
        );
        assertThrows(CodecException.class, () ->
                encoder.encode(ctx));

        when(request.contentType()).thenReturn(MediaType.APPLICATION_FORM_URLENCODED);
        then(((MultipartBody) encoder.encode(ctx).value()).multipartEncode()).isFalse();
    }

}
