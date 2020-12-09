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
package esa.httpclient.core.netty;

import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2FrameListener;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class Http2ConnectionHandlerBuilderTest {

    @Test
    void testBuild() {
        final HandleRegistry registry = new HandleRegistry(2, 1);
        final Http2ConnectionHandlerBuilder builder = new Http2ConnectionHandlerBuilder(registry);

        assertThrows(NullPointerException.class, () -> builder.frameListener(null));
        assertThrows(IllegalArgumentException.class, () -> builder.gracefulShutdownTimeoutMillis(-2L));

        final Http2FrameListener listener = mock(Http2FrameListener.class);
        builder.frameListener(listener);

        final Http2ConnectionDecoder decoder = mock(Http2ConnectionDecoder.class);
        final Http2ConnectionEncoder encoder = mock(Http2ConnectionEncoder.class);

        builder.codec(decoder, encoder);
        builder.gracefulShutdownTimeoutMillis(31L);

        Http2ConnectionHandler handler = builder.build();
        then(handler.encoder()).isSameAs(encoder);
        then(handler.gracefulShutdownTimeoutMillis()).isEqualTo(31L);
        then(handler.getRegistry()).isSameAs(registry);
    }
}
