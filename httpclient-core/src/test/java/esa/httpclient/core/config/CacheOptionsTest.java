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
package esa.httpclient.core.config;

import org.junit.jupiter.api.Test;

import java.util.StringJoiner;

import static org.assertj.core.api.BDDAssertions.then;

class CacheOptionsTest {

    @Test
    void testCustom() {
        final CacheOptions options = CacheOptions.options().expireSeconds(1L)
                .initialCapacity(2).maximumSize(3).build();
        then(options.expireSeconds()).isEqualTo(1L);
        then(options.initialCapacity()).isEqualTo(2);
        then(options.maximumSize()).isEqualTo(3L);
        then(options.toString()).isEqualTo(new StringJoiner(", ", CacheOptions.class.getSimpleName() + "[", "]")
                .add("initialCapacity=" + 2)
                .add("maximumSize=" + 3L)
                .add("expireSeconds=" + 1L)
                .toString());
    }

    @Test
    void testCopy() {
        final CacheOptions options = CacheOptions.options()
                .initialCapacity(1)
                .maximumSize(2)
                .expireSeconds(3L)
                .build();
        then(options.initialCapacity()).isEqualTo(1);
        then(options.maximumSize()).isEqualTo(2L);
        then(options.expireSeconds()).isEqualTo(3L);
    }
}
