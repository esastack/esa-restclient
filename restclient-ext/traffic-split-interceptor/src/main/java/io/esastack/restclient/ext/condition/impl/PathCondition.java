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
import io.esastack.restclient.ext.matcher.StringMatcher;

public class PathCondition implements RequestRedefineCondition {
    private final StringMatcher matcher;

    public PathCondition(StringMatcher matcher) {
        Checks.checkNotNull(matcher, "matcher");
        this.matcher = matcher;
    }

    @Override
    public MatchResult match(RestRequest request) {
        return matcher.match(request.path());
    }

    @Override
    public String toString() {
        return "PathCondition{" +
                "matcher=" + matcher +
                '}';
    }
}
