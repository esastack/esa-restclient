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

import esa.commons.function.ThrowingSupplier;
import esa.httpclient.core.config.ChannelPoolOptions;
import io.netty.handler.ssl.SslHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ChannelPoolTest {

    @Test
    void testConstructor() {
        final ThrowingSupplier<SslHandler> ssl = () -> null;
        final io.netty.channel.pool.ChannelPool pool = mock(io.netty.channel.pool.ChannelPool.class);
        final ChannelPoolOptions options = mock(ChannelPoolOptions.class);

        assertThrows(NullPointerException.class, () -> new ChannelPool(null,
                options, true, ssl));

        assertThrows(NullPointerException.class, () -> new ChannelPool(
                pool, null, true, ssl));

        assertThrows(NullPointerException.class, () -> new ChannelPool(
                pool, options, true, null));

        new ChannelPool(pool, options, true, ssl);
    }
}
