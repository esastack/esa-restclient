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

import io.esastack.restclient.RestRequest;

import java.util.Map;

public class ParamMatcher {
    private Map<String, StringMatcher> paramMap;

    public ParamMatcher() {
    }

    public MatchResult match(RestRequest request) {
        if (paramMap != null) {
            for (Map.Entry<String, StringMatcher> param : paramMap.entrySet()) {
                String name = param.getKey();
                if (name == null) {
                    continue;
                }
                StringMatcher value = param.getValue();
                if (value == null) {
                    if (request.paramNames().contains(name)) {
                        continue;
                    } else {
                        return MatchResult.fail("Params don't contain name:" + name);
                    }
                }
                MatchResult result = value.match(request.getParam(name));
                if (!result.isMatch()) {
                    return result;
                }
            }
        }

        return MatchResult.success();
    }

    public Map<String, StringMatcher> getParams() {
        return paramMap;
    }

    public void setParams(Map<String, StringMatcher> params) {
        this.paramMap = params;
    }

    @Override
    public String toString() {
        return "ParamMatcher{" +
                "paramMap=" + paramMap +
                '}';
    }
}