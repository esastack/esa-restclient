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

import static org.assertj.core.api.BDDAssertions.then;

class Http1OptionsTest {

    @Test
    void testDefault() {
        final Http1Options options = Http1Options.ofDefault();
        then(options.maxInitialLineLength()).isEqualTo(4096);
        then(options.maxHeaderSize()).isEqualTo(8192);
        then(options.maxChunkSize()).isEqualTo(8192);
    }

    @Test
    void testCustom() {
        final Http1Options options = Http1Options.options().maxInitialLineLength(1)
                .maxHeaderSize(2).maxChunkSize(3).build();
        then(options.maxInitialLineLength()).isEqualTo(1);
        then(options.maxHeaderSize()).isEqualTo(2);
        then(options.maxChunkSize()).isEqualTo(3);
    }

    @Test
    void testCopy() {
        final Http1Options options = Http1Options.ofDefault().copy();
        then(options.maxInitialLineLength()).isEqualTo(4096);
        then(options.maxHeaderSize()).isEqualTo(8192);
        then(options.maxChunkSize()).isEqualTo(8192);
    }

}
