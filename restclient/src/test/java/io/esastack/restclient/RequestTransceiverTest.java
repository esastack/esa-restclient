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
package io.esastack.restclient;

import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpStatus;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.HttpUri;
import io.esastack.restclient.codec.impl.ByteToByteCodec;
import io.esastack.restclient.codec.impl.StringCodec;
import io.esastack.restclient.exec.RestRequestExecutor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestTransceiverTest {

    @Test
    void testProceed() throws ExecutionException, InterruptedException {
        RequestTransceiver requestTransceiver = new RequestTransceiver();
        RestRequest request0 = mock(RestRequest.class);
        when(request0.uri()).thenReturn(new HttpUri("aaa"));
        assertThrows(IllegalStateException.class,
                () -> requestTransceiver.proceed(request0));

        RestCompositeRequest request = RequestMockUtil.mockRequest(
                mock(RestClientOptions.class),
                mock(RestRequestExecutor.class),
                new ByteToByteCodec(),
                new StringCodec(),
                "Hi".getBytes(),
                "Hi",
                "aaa",
                "aaa");
        RestResponse response = requestTransceiver.proceed(request).toCompletableFuture().get();
        then(response.status()).isEqualTo(HttpStatus.OK.code());
        then(response.headers().get(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(MediaType.TEXT_PLAIN.toString());
        then(response.cookies().size())
                .isEqualTo(1);
        then(response.cookies().iterator().next().value())
                .isEqualTo("aaa");
    }
}
