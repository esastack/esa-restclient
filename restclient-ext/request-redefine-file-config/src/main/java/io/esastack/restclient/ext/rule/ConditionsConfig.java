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
import io.esastack.restclient.ext.condition.RequestRedefineCondition;
import io.esastack.restclient.ext.condition.impl.*;

import java.util.ArrayList;
import java.util.List;

public class ConditionsConfig {
    private String method;
    private StringMatcher authority;
    private StringMatcher path;
    private HeaderMatcher header;
    private ParamMatcher param;

    public void setMethod(String method) {
        this.method = method;
    }

    public void setAuthority(StringMatcher authority) {
        this.authority = authority;
    }

    public void setPath(StringMatcher path) {
        this.path = path;
    }

    public void setHeader(HeaderMatcher header) {
        this.header = header;
    }

    public void setParam(ParamMatcher param) {
        this.param = param;
    }

    public List<RequestRedefineCondition> build() {
        List<RequestRedefineCondition> conditions = new ArrayList<>();
        if (StringUtils.isNotBlank(method)) {
            conditions.add(new MethodCondition(method));
        }
        if (authority != null) {
            conditions.add(new AuthorityCondition(authority));
        }
        if (path != null) {
            conditions.add(new PathCondition(path));
        }
        if (param != null) {
            conditions.add(new ParamCondition(param));
        }
        if (header != null) {
            conditions.add(new HeaderCondition(header));
        }
        return conditions;
    }

    @Override
    public String toString() {
        return "ConditionsConfig{" +
                "method='" + method + '\'' +
                ", authority=" + authority +
                ", path=" + path +
                ", header=" + header +
                ", param=" + param +
                '}';
    }
}
