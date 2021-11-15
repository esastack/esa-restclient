/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.httpclient.core.netty;

import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.HttpMessage;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.BDDAssertions.then;

class NettyResponseTest {

    @Test
    void testBasic() {
        final int status = ThreadLocalRandom.current().nextInt(100, 500);
        final boolean aggregated = ThreadLocalRandom.current().nextBoolean();
        final byte[] data = new byte[0];

        final NettyResponse response = new NettyResponse(aggregated);
        final HttpMessage message = new HttpMessageImpl(status, HttpVersion.HTTP_2, new Http1HeadersImpl());
        response.message(message);
        response.body(BufferUtil.buffer(data));

        response.headers().add("A", "B");
        response.trailers().add("X", "Y");
        then(response.body().readableBytes()).isEqualTo(data.length);
        then(response.version()).isEqualTo(HttpVersion.HTTP_2);

        then(response.headers().get("A")).isEqualTo("B");
        then(response.trailers().get("A")).isNull();

        then(response.headers().get("X")).isNull();
        then(response.trailers().get("X")).isEqualTo("Y");

        then(response.aggregated()).isEqualTo(aggregated);
    }
}
