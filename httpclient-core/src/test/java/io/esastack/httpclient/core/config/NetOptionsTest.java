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
package io.esastack.httpclient.core.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class NetOptionsTest {

    @Test
    void testDefault() {
        final NetOptions options = NetOptions.ofDefault();
        then(options.soSndBuf()).isEqualTo(-1);
        then(options.soRcvBuf()).isEqualTo(-1);
        then(options.isSoKeepAlive()).isTrue();
        then(options.isSoReuseAddr()).isFalse();
        then(options.isTcpNoDelay()).isTrue();
        then(options.soLinger()).isEqualTo(-1);
        then(options.writeBufferHighWaterMark()).isEqualTo(-1);
        then(options.writeBufferLowWaterMark()).isEqualTo(-1);
    }

    @Test
    void testCustom() {
        final NetOptions options = NetOptions.options().soSndBuf(1)
                .soRcvBuf(2)
                .soKeepAlive(false)
                .soReuseAddr(true)
                .tcpNoDelay(false)
                .soLinger(3)
                .writeBufferHighWaterMark(4)
                .writeBufferLowWaterMark(5).build();

        then(options.soSndBuf()).isEqualTo(1);
        then(options.soRcvBuf()).isEqualTo(2);
        then(options.isSoKeepAlive()).isFalse();
        then(options.isSoReuseAddr()).isTrue();
        then(options.isTcpNoDelay()).isFalse();
        then(options.soLinger()).isEqualTo(3);
        then(options.writeBufferHighWaterMark()).isEqualTo(4);
        then(options.writeBufferLowWaterMark()).isEqualTo(5);
    }

    @Test
    void testCopy() {
        final NetOptions options = NetOptions.ofDefault().copy();

        then(options.soSndBuf()).isEqualTo(-1);
        then(options.soRcvBuf()).isEqualTo(-1);
        then(options.isSoKeepAlive()).isTrue();
        then(options.isSoReuseAddr()).isFalse();
        then(options.isTcpNoDelay()).isTrue();
        then(options.soLinger()).isEqualTo(-1);
        then(options.writeBufferHighWaterMark()).isEqualTo(-1);
        then(options.writeBufferLowWaterMark()).isEqualTo(-1);
    }

}
