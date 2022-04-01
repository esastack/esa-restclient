package io.esastack.restclient.ext.interceptor;

import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.exec.InvocationChain;
import io.esastack.restclient.exec.RestInterceptor;
import io.esastack.restclient.ext.RedefineContextImpl;
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
            if (rule.matchMechanism().match(rule.name(), request, rule.conditions())) {
                return new RedefineContextImpl(request, rule.actions(), next).next();
            }
        }
        return next.proceed(request);
    }
}
