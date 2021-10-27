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

import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.exec.RetryPredicate;
import io.esastack.httpclient.core.exec.RetryPredicateImpl;
import org.junit.jupiter.api.Test;

import java.util.StringJoiner;
import java.util.function.IntToLongFunction;

import static org.assertj.core.api.BDDAssertions.then;

class RetryOptionsTest {

    @Test
    void testDefault() {
        final RetryOptions options = RetryOptions.ofDefault();
        then(options.maxRetries()).isEqualTo(3);
        then(options.predicate()).isSameAs(RetryPredicateImpl.DEFAULT);
        then(options.intervalMillis()).isNull();
        then(options.toString()).isEqualTo(new StringJoiner(", ", RetryOptions.class.getSimpleName() + "[", "]")
                .add("maxRetries=" + 3)
                .add("predicate=" + RetryPredicateImpl.DEFAULT)
                .add("intervalMillis=" + null)
                .toString());
    }

    @Test
    void testCustom() {
        final RetryPredicate predicate = new RetryPredicateImpl() {
            @Override
            protected boolean canRetry0(HttpRequest request, HttpResponse response, Context ctx, Throwable cause) {
                return false;
            }
        };

        final IntToLongFunction intervalMs = value -> 0;

        final RetryOptions options = RetryOptions.options().maxRetries(5)
                .predicate(predicate).intervalMs(intervalMs).build();
        then(options.maxRetries()).isEqualTo(5);
        then(options.predicate()).isSameAs(predicate);
        then(options.intervalMillis()).isSameAs(intervalMs);
    }

    @Test
    void testCopy() {
        final RetryOptions options = RetryOptions.ofDefault().copy();
        then(options.maxRetries()).isEqualTo(3);
        then(options.predicate()).isSameAs(RetryPredicateImpl.DEFAULT);
        then(options.intervalMillis()).isNull();
    }

}
