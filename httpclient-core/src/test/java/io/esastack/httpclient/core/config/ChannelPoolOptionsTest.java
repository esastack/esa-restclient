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

class ChannelPoolOptionsTest {

    @Test
    void testDefault() {
        final ChannelPoolOptions options = ChannelPoolOptions.ofDefault();
        then(options.connectTimeout()).isEqualTo(3000L);
        then(options.poolSize()).isEqualTo(512);
        then(options.waitingQueueLength()).isEqualTo(256);
        then(options.readTimeout()).isEqualTo(6000);
    }

    @Test
    void testCustom() {
        final ChannelPoolOptions options = ChannelPoolOptions.options().connectTimeout(1)
                .poolSize(2).waitingQueueLength(3).readTimeout(4).build();
        then(options.connectTimeout()).isEqualTo(1L);
        then(options.poolSize()).isEqualTo(2);
        then(options.waitingQueueLength()).isEqualTo(3);
        then(options.readTimeout()).isEqualTo(4);
    }

    @Test
    void testCopy() {
        final ChannelPoolOptions options = ChannelPoolOptions.ofDefault().copy();
        then(options.connectTimeout()).isEqualTo(3000L);
        then(options.poolSize()).isEqualTo(512);
        then(options.waitingQueueLength()).isEqualTo(256);
        then(options.readTimeout()).isEqualTo(6000);
    }

}
