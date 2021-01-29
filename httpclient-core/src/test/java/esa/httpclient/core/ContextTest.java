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
package esa.httpclient.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6BDDAssertions.then;

class ContextTest {

    @Test
    void testBasic() {
        final Context ctx = new Context();
        then(ctx.isUseExpectContinue()).isFalse();
        then(ctx.maxRedirects()).isEqualTo(0);
        then(ctx.maxRetries()).isEqualTo(0);

        ctx.useExpectContinue(true);
        ctx.maxRetries(10);
        ctx.maxRedirects(100);
        then(ctx.isUseExpectContinue()).isTrue();
        then(ctx.maxRedirects()).isEqualTo(100);
        then(ctx.maxRetries()).isEqualTo(10);
    }

}
