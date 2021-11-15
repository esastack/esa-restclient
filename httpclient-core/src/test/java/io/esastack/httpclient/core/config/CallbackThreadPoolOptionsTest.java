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

class CallbackThreadPoolOptionsTest {

    @Test
    void testDefault() {
        then(CallbackThreadPoolOptions.ofDefault()).isNull();

        // case 1: if coreSize is absent, coreSize = maxSize
        final CallbackThreadPoolOptions options1 = CallbackThreadPoolOptions.options()
                .maxSize(2)
                .blockingQueueLength(3)
                .keepAliveSeconds(4L)
                .gracefullyShutdownSeconds(5L)
                .build();
        then(options1.coreSize()).isEqualTo(2);
        then(options1.maxSize()).isEqualTo(2);
        then(options1.blockingQueueLength()).isEqualTo(3);
        then(options1.keepAliveSeconds()).isEqualTo(4L);
        then(options1.gracefullyShutdownSeconds()).isEqualTo(5L);
        then(options1.toString()).isEqualTo(
                new StringJoiner(", ", CallbackThreadPoolOptions.class.getSimpleName() + "[", "]")
                        .add("coreSize=" + 2)
                        .add("maxSize=" + 2)
                        .add("blockingQueueLength=" + 3)
                        .add("keepAliveSeconds=" + 4L)
                        .add("gracefullyShutdownSeconds=" + 5L)
                        .toString()
        );

        // case 2: if maxSize is absent, maxSize = coreSize
        final CallbackThreadPoolOptions options2 = CallbackThreadPoolOptions
                .options().coreSize(1)
                .blockingQueueLength(3)
                .keepAliveSeconds(4L)
                .gracefullyShutdownSeconds(5L)
                .build();
        then(options2.coreSize()).isEqualTo(1);
        then(options2.maxSize()).isEqualTo(1);
        then(options2.blockingQueueLength()).isEqualTo(3);
        then(options2.keepAliveSeconds()).isEqualTo(4L);
        then(options2.gracefullyShutdownSeconds()).isEqualTo(5L);

        // case 3: coreSize and maxSize are all present
        final CallbackThreadPoolOptions options3 = CallbackThreadPoolOptions.options()
                .coreSize(1)
                .maxSize(2)
                .blockingQueueLength(3)
                .keepAliveSeconds(4L)
                .gracefullyShutdownSeconds(5L)
                .build();
        then(options3.coreSize()).isEqualTo(1);
        then(options3.maxSize()).isEqualTo(2);
        then(options3.blockingQueueLength()).isEqualTo(3);
        then(options3.keepAliveSeconds()).isEqualTo(4L);
        then(options3.gracefullyShutdownSeconds()).isEqualTo(5L);
    }

    @Test
    void testCopy() {
        final CallbackThreadPoolOptions options = CallbackThreadPoolOptions.options()
                .coreSize(1)
                .maxSize(2)
                .blockingQueueLength(3)
                .keepAliveSeconds(4L)
                .gracefullyShutdownSeconds(5L)
                .build();
        then(options.coreSize()).isEqualTo(1);
        then(options.maxSize()).isEqualTo(2);
        then(options.blockingQueueLength()).isEqualTo(3);
        then(options.keepAliveSeconds()).isEqualTo(4L);
        then(options.gracefullyShutdownSeconds()).isEqualTo(5L);
    }
}
