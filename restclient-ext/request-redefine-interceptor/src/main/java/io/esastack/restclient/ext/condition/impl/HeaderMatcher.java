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

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.restclient.ext.condition.MatchResult;

public class HeaderMatcher {
    private String contains;
    private String name;
    private StringMatcher value;

    public HeaderMatcher() {
    }

    public MatchResult match(HttpHeaders headers) {
        if (contains != null) {
            if (headers.contains(contains)) {
                return MatchResult.success();
            }
        }
        if (name != null) {
            String headerValue = headers.get(name);
            if (headerValue != null) {
                return value.match(headerValue);
            }
        }

        return MatchResult.fail("Header don't meet expectations("
                + "contains='" + contains + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' + ")!");
    }

    public void setContains(String contains) {
        this.contains = contains;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(StringMatcher value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "HeaderMatcher{" +
                "contains='" + contains + '\'' +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
