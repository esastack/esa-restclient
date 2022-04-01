package io.esastack.restclient.ext.rule;

import esa.commons.Checks;
import io.esastack.httpclient.core.util.LoggerUtils;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.ext.action.RequestRedefineAction;
import io.esastack.restclient.ext.condition.MatchResult;
import io.esastack.restclient.ext.condition.RequestRedefineCondition;

import java.util.List;

public interface RedefineRule {

    String name();

    MatchMechanism matchMechanism();

    List<RequestRedefineCondition> conditions();

    List<RequestRedefineAction> actions();

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

        public abstract boolean match(String ruleName, RestRequest
                request, List<RequestRedefineCondition> conditions);
    }
}
