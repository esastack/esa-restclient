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

import esa.commons.Checks;
import io.esastack.httpclient.core.util.LoggerUtils;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.ext.action.RequestRedefineAction;
import io.esastack.restclient.ext.condition.RequestRedefineCondition;
import io.esastack.restclient.ext.matcher.MatchResult;

import java.util.List;

public interface RedefineRule {

    String name();

    /**
     * Determine the matching mechanism of conditions.
     *
     * @return MatchMechanism
     */
    MatchMechanism matchMechanism();

    List<RequestRedefineCondition> conditions();

    List<RequestRedefineAction> actions();

    default boolean match(RestRequest request) {
        return matchMechanism().match(name(), request, conditions());
    }

    enum MatchMechanism {
        ANY {
            @Override
            public boolean match(String ruleName, RestRequest request, List<RequestRedefineCondition> conditions) {
                Checks.checkNotNull(request, "request");
                Checks.checkNotNull(conditions, "conditions");
                for (RequestRedefineCondition condition : conditions) {
                    if (condition.match(request).isMatch()) {
                        if (LoggerUtils.logger().isDebugEnabled()) {
                            LoggerUtils.logger().debug("Request({}) hit redefineRule({})"
                                            + ", cause matchMechanism is ANY"
                                            + ", and request match condition({})",
                                    request, ruleName, condition);
                        }
                        return true;
                    }
                }
                if (LoggerUtils.logger().isDebugEnabled()) {
                    LoggerUtils.logger().debug("Request({}) don't hit redefineRule({})"
                                    + ", cause matchMechanism is ANY"
                                    + ", and request don't match any condition.",
                            request, ruleName);
                }
                return false;
            }
        },

        ALL {
            @Override
            public boolean match(String ruleName, RestRequest request, List<RequestRedefineCondition> conditions) {
                Checks.checkNotNull(request, "request");
                Checks.checkNotNull(conditions, "conditions");
                for (RequestRedefineCondition condition : conditions) {
                    MatchResult result = condition.match(request);
                    if (!result.isMatch()) {
                        if (LoggerUtils.logger().isDebugEnabled()) {
                            LoggerUtils.logger().debug("Request({}) don't hit redefineRule({})"
                                            + ", cause matchMechanism is ALL"
                                            + ", but it don't match the condition({}), unMatchReason: {}",
                                    request, ruleName, condition, result.unMatchReason());
                        }
                        return false;
                    }
                }

                if (LoggerUtils.logger().isDebugEnabled()) {
                    LoggerUtils.logger().debug("Request({}) hit redefineRule({})"
                                    + ", cause matchMechanism is ALL"
                                    + ", and request match all conditions.",
                            request, ruleName);
                }
                return true;
            }
        },

        NOT {
            @Override
            public boolean match(String ruleName, RestRequest request, List<RequestRedefineCondition> conditions) {
                Checks.checkNotNull(request, "request");
                Checks.checkNotNull(conditions, "conditions");
                for (RequestRedefineCondition condition : conditions) {
                    MatchResult result = condition.match(request);
                    if (result.isMatch()) {
                        if (LoggerUtils.logger().isDebugEnabled()) {
                            LoggerUtils.logger().debug("Request({}) don't hit redefineRule({})"
                                            + ", cause matchMechanism is NOT"
                                            + ", but request match condition({})",
                                    request, ruleName, condition);
                        }
                        return false;
                    }
                }
                if (LoggerUtils.logger().isDebugEnabled()) {
                    LoggerUtils.logger().debug("Request({}) hit redefineRule({})"
                                    + ", cause matchMechanism is NOT"
                                    + ", and request don't match any condition.",
                            request, ruleName);
                }
                return true;
            }
        };

        public abstract boolean match(String ruleName, RestRequest request,
                                      List<RequestRedefineCondition> conditions);
    }
}
