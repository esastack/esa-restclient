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

import java.util.Map;

public class HeaderMatcher {
    private Map<String, StringMatcher> headerMap;

    public HeaderMatcher() {
    }

    public MatchResult match(HttpHeaders headers) {
        if (headerMap != null) {
            for (Map.Entry<String, StringMatcher> header : headerMap.entrySet()) {
                String name = header.getKey();
                if (name == null) {
                    continue;
                }
                StringMatcher value = header.getValue();
                if (value == null) {
                    if (headers.contains(name)) {
                        continue;
                    } else {
                        return MatchResult.fail("Headers don't contain name:" + name);
                    }
                }
                MatchResult result = value.match(headers.get(name));
                if (!result.isMatch()) {
                    return result;
                }
            }
        }

        return MatchResult.success();
    }

    public Map<String, StringMatcher> getHeaders() {
        return headerMap;
    }

    public void setHeaders(Map<String, StringMatcher> headers) {
        this.headerMap = headers;
    }

    @Override
    public String toString() {
        return "HeaderMatcher{" +
                "headerMap=" + headerMap +
                '}';
    }
}
