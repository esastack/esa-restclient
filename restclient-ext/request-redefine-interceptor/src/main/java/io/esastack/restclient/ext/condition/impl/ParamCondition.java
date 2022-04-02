package io.esastack.restclient.ext.condition.impl;

import esa.commons.Checks;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.ext.condition.MatchResult;
import io.esastack.restclient.ext.condition.RequestRedefineCondition;

public class ParamCondition implements RequestRedefineCondition {

    private final ParamMatcher matcher;

    public ParamCondition(ParamMatcher matcher) {
        Checks.checkNotNull(matcher, "matcher");
        this.matcher = matcher;
    }

    @Override
    public MatchResult match(RestRequest request) {
        return matcher.match(request);
    }
}
