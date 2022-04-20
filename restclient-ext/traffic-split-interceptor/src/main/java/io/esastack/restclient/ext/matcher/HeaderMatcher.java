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
package io.esastack.restclient.ext.matcher;

import io.esastack.commons.net.http.HttpHeaders;

import java.util.Arrays;

public class HeaderMatcher {
    private String[] contains;
    private String name;
    private StringMatcher value;

    public HeaderMatcher() {
    }

    public MatchResult match(HttpHeaders headers) {
        if (contains != null) {
            for (String contain : contains) {
                if (!headers.contains(contain)) {
                    return MatchResult.fail("Headers don,t contain " + contain);
                }
            }
            return MatchResult.success();
        }
        if (name != null && value != null) {
            return value.match(headers.get(name));
        }

        return MatchResult.fail("Header don't meet expectations("
                + "contains='" + Arrays.toString(contains) + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' + ")!");
    }

    public void setContains(String contains) {
        if (contains == null) {
            this.contains = null;
        } else {
            this.contains = contains.split(",");
        }
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
                "contains='" + Arrays.toString(contains) + '\'' +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
