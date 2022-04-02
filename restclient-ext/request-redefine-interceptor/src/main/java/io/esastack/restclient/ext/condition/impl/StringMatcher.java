/*
 * Copyright 2022 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restclient.ext.condition.impl;

import io.esastack.restclient.ext.condition.MatchResult;

public class StringMatcher {
    private String exact;
    private String prefix;
    private String regex;

    public StringMatcher() {
    }

    public MatchResult match(String actual) {
        if (actual == null) {
            actual = "";
        }

        if (exact != null) {
            if (exact.equals(actual)) {
                return MatchResult.success();
            }
        }
        if (prefix != null) {
            if (actual.startsWith(prefix)) {
                return MatchResult.success();
            }
        }

        if (regex != null) {
            if (actual.matches(regex)) {
                return MatchResult.success();
            }
        }
        return MatchResult.fail("Actual(" + actual + ") don't meet expectations("
                + "exact='" + exact + '\'' +
                ", prefix='" + prefix + '\'' +
                ", regex='" + regex + '\'' + ")!");
    }

    public void setExact(String exact) {
        this.exact = exact;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public String toString() {
        return "StringMatcher{" +
                "exact='" + exact + '\'' +
                ", prefix='" + prefix + '\'' +
                ", regex='" + regex + '\'' +
                '}';
    }
}
