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

import esa.commons.Checks;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.ext.condition.RequestRedefineCondition;
import io.esastack.restclient.ext.matcher.MatchResult;

public class MethodCondition implements RequestRedefineCondition {

    private final String method;

    public MethodCondition(String method) {
        Checks.checkNotEmptyArg(method, "Method should not be empty!");
        this.method = method;
    }

    @Override
    public MatchResult match(RestRequest request) {
        String actualMethod = request.method().toString();
        if (method.equalsIgnoreCase(actualMethod)) {
            return MatchResult.success();
        } else {
            return MatchResult.fail("Method don't match," +
                    " expect : " + method + ",actual : " + actualMethod + ".");
        }
    }

    @Override
    public String toString() {
        return "MethodCondition{" +
                "method='" + method + '\'' +
                '}';
    }
}
