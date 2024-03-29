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
package io.esastack.restclient.ext.rule;

import esa.commons.StringUtils;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.ext.condition.TrafficSplitCondition;
import io.esastack.restclient.ext.condition.impl.AuthorityCondition;
import io.esastack.restclient.ext.condition.impl.HeaderCondition;
import io.esastack.restclient.ext.condition.impl.MethodCondition;
import io.esastack.restclient.ext.condition.impl.ParamCondition;
import io.esastack.restclient.ext.condition.impl.PathCondition;
import io.esastack.restclient.ext.matcher.HeaderMatcher;
import io.esastack.restclient.ext.matcher.KVMatcher;
import io.esastack.restclient.ext.matcher.MatchResult;
import io.esastack.restclient.ext.matcher.ParamMatcher;
import io.esastack.restclient.ext.matcher.StringMatcher;

import java.util.ArrayList;
import java.util.List;

public class ConditionConfig {
    private String method;
    private StringMatcher uriAuthority;
    private StringMatcher path;
    private List<KVMatcher> headers;
    private List<KVMatcher> params;

    public void setMethod(String method) {
        this.method = method;
    }

    public void setUriAuthority(StringMatcher uriAuthority) {
        this.uriAuthority = uriAuthority;
    }

    public void setPath(StringMatcher path) {
        this.path = path;
    }

    public void setHeaders(List<KVMatcher> headers) {
        this.headers = headers;
    }

    public void setParams(List<KVMatcher> params) {
        this.params = params;
    }

    public TrafficSplitCondition build() {
        List<TrafficSplitCondition> conditions = new ArrayList<>(3);
        if (StringUtils.isNotBlank(method)) {
            conditions.add(new MethodCondition(method));
        }
        if (uriAuthority != null) {
            conditions.add(new AuthorityCondition(uriAuthority));
        }
        if (path != null) {
            conditions.add(new PathCondition(path));
        }
        if (params != null) {
            conditions.add(new ParamCondition(new ParamMatcher(params)));
        }
        if (headers != null) {
            conditions.add(new HeaderCondition(new HeaderMatcher(headers)));
        }
        return new AggregateCondition(conditions);
    }

    @Override
    public String toString() {
        return "ConditionsConfig{" +
                "method='" + method + '\'' +
                ", uriAuthority=" + uriAuthority +
                ", path=" + path +
                ", headers=" + headers +
                ", params=" + params +
                '}';
    }

    private static final class AggregateCondition implements TrafficSplitCondition {
        private final List<TrafficSplitCondition> conditions;

        private AggregateCondition(List<TrafficSplitCondition> conditions) {
            this.conditions = conditions;
        }

        @Override
        public MatchResult match(RestRequest request) {
            for (TrafficSplitCondition condition : conditions) {
                MatchResult result = condition.match(request);
                if (!result.isMatch()) {
                    return result;
                }
            }
            return MatchResult.success();
        }

        @Override
        public String toString() {
            return "AggregateCondition{" +
                    "conditions=" + conditions +
                    '}';
        }
    }
}
