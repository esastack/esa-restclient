package io.esastack.restclient.ext.interceptor;

import esa.commons.spi.SpiLoader;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.exec.InvocationChain;
import io.esastack.restclient.exec.RestInterceptor;
import io.esastack.restclient.ext.RedefineContextImpl;
import io.esastack.restclient.ext.rule.RedefineRule;
import io.esastack.restclient.ext.rule.RedefineRulesSource;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class RequestRedefineInterceptor implements RestInterceptor {

    private final RedefineRulesSource rulesSource;

    public RequestRedefineInterceptor() {
        List<RedefineRulesSource> rulesSources = SpiLoader
                .cached(RedefineRulesSource.class)
                .getAll();
        int sourcesSize = rulesSources.size();
        if (sourcesSize == 0 || sourcesSize > 1) {
            throw new IllegalStateException("Unexpected size of rulesSources : " + sourcesSize
                    + ", expected size is 1. rulesSources: " + rulesSources);
        }
        this.rulesSource = rulesSources.get(0);
    }

    @Override
    public CompletionStage<RestResponse> proceed(RestRequest request, InvocationChain next) {
        List<RedefineRule> rules = rulesSource.rules();
        for (RedefineRule rule : rules) {
            if (rule.matchMechanism().match(rule.name(), request, rule.conditions())) {
                return new RedefineContextImpl(request, rule.actions(), next).next();
            }
        }
        return next.proceed(request);
    }
}
