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

import esa.commons.Checks;
import io.esastack.httpclient.core.Reusable;
import io.esastack.httpclient.core.exec.RetryPredicate;
import io.esastack.httpclient.core.exec.RetryPredicateImpl;

import java.io.Serializable;
import java.util.StringJoiner;
import java.util.function.IntToLongFunction;

public class RetryOptions implements Reusable<RetryOptions>, Serializable {

    private static final long serialVersionUID = -4016995314366331767L;

    private final int maxRetries;
    private final transient RetryPredicate predicate;
    private final transient IntToLongFunction intervalMillis;

    private RetryOptions(int maxRetries,
                         RetryPredicate predicate,
                         IntToLongFunction intervalMillis) {
        Checks.checkNotNull(predicate, "predicate");
        Checks.checkArg(maxRetries >= 1, "maxRetries is " + maxRetries +
                " (expected >= 1)");
        this.maxRetries = maxRetries;
        this.predicate = predicate;
        this.intervalMillis = intervalMillis;
    }

    public static RetryOptions ofDefault() {
        return new RetryOptionsBuilder().build();
    }

    public static RetryOptionsBuilder options() {
        return new RetryOptionsBuilder();
    }

    @Override
    public RetryOptions copy() {
        return new RetryOptions(maxRetries, predicate, intervalMillis);
    }

    public int maxRetries() {
        return maxRetries;
    }

    public RetryPredicate predicate() {
        return predicate;
    }

    public IntToLongFunction intervalMillis() {
        return this.intervalMillis;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RetryOptions.class.getSimpleName() + "[", "]")
                .add("maxRetries=" + maxRetries)
                .add("predicate=" + predicate)
                .add("intervalMillis=" + intervalMillis)
                .toString();
    }

    public static class RetryOptionsBuilder {

        private int maxRetries = 3;
        private RetryPredicate predicate = RetryPredicateImpl.DEFAULT;
        private IntToLongFunction intervalMillis;

        RetryOptionsBuilder() {
        }

        public RetryOptionsBuilder intervalMs(IntToLongFunction intervalMillis) {
            this.intervalMillis = intervalMillis;
            return this;
        }

        public RetryOptionsBuilder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public RetryOptionsBuilder predicate(RetryPredicate predicate) {
            this.predicate = predicate;
            return this;
        }

        public RetryOptions build() {
            return new RetryOptions(maxRetries, predicate, intervalMillis);
        }

    }

}
