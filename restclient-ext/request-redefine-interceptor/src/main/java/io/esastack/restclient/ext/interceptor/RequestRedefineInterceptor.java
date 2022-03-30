package io.esastack.restclient.ext.interceptor;

import io.esastack.httpclient.core.util.LoggerUtils;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.exec.InvocationChain;
import io.esastack.restclient.exec.RestInterceptor;
import io.esastack.restclient.ext.RedefineContextImpl;
import io.esastack.restclient.ext.condition.MatchResult;
import io.esastack.restclient.ext.condition.RequestRedefineCondition;
import io.esastack.restclient.ext.rule.RedefineRule;
import io.esastack.restclient.ext.rule.RedefineRulesManager;
import io.esastack.restclient.ext.rule.impl.RedefineRulesManagerImpl;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class RequestRedefineInterceptor implements RestInterceptor {

    private final RedefineRulesManager rulesManager = new RedefineRulesManagerImpl();

    @Override
    public CompletionStage<RestResponse> proceed(RestRequest request, InvocationChain next) {
        List<RedefineRule> rules = rulesManager.rules();
        for (RedefineRule rule : rules) {
            boolean match = true;
            for (RequestRedefineCondition condition : rule.conditions()) {
                MatchResult result = condition.match(request);
                if (!result.isMatch()) {
                    match = false;
                    if (LoggerUtils.logger().isDebugEnabled()) {
                        LoggerUtils.logger().debug("Request({}) don,t hit redefineRule({}), cause:{}",
                                request, rule.name(), result.unMatchReason());
                    }
                    break;
                }
            }
            if (match) {
                if (LoggerUtils.logger().isDebugEnabled()) {
                    LoggerUtils.logger().debug("Request({}) hit redefineRule({}).",
                            request, rule.name());
                }
                return new RedefineContextImpl(request, rule.actions(), next).next();
            }
        }
        return next.proceed(request);
    }
}
