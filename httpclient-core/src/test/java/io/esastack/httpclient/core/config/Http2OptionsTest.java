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

import java.util.StringJoiner;

import static org.assertj.core.api.BDDAssertions.then;

class Http2OptionsTest {

    @Test
    void testDefault() {
        final Http2Options options = Http2Options.ofDefault();
        then(options.maxReservedStreams()).isEqualTo(100);
        then(options.maxFrameSize()).isEqualTo(16384);
        then(options.gracefulShutdownTimeoutMillis()).isEqualTo(30_000L);
        then(options.toString()).isEqualTo(new StringJoiner(", ", Http2Options.class.getSimpleName() + "[", "]")
                .add("maxReservedStreams=" + 100)
                .add("maxFrameSize=" + 16384)
                .add("gracefulShutdownTimeoutMillis=" + 30_000L)
                .toString());
    }

    @Test
    void testCustom() {
        final Http2Options options = Http2Options.options().maxReservedStreams(1)
                .maxFrameSize(16385).gracefulShutdownTimeoutMillis(3L).build();
        then(options.maxReservedStreams()).isEqualTo(1);
        then(options.maxFrameSize()).isEqualTo(16385);
        then(options.gracefulShutdownTimeoutMillis()).isEqualTo(3L);
    }

    @Test
    void testCopy() {
        final Http2Options options = Http2Options.ofDefault().copy();
        then(options.maxReservedStreams()).isEqualTo(100);
        then(options.maxFrameSize()).isEqualTo(16384);
        then(options.gracefulShutdownTimeoutMillis()).isEqualTo(30_000L);
    }

}
