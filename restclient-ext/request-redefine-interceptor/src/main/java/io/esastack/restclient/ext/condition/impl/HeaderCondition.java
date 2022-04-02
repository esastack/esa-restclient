package io.esastack.restclient.ext.condition.impl;

import esa.commons.Checks;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.ext.condition.MatchResult;
import io.esastack.restclient.ext.condition.RequestRedefineCondition;

public class HeaderCondition implements RequestRedefineCondition {

    private final HeaderMatcher matcher;

    public HeaderCondition(HeaderMatcher matcher) {
        Checks.checkNotNull(matcher, "matcher");
        this.matcher = matcher;
    }

    @Override
    public MatchResult match(RestRequest request) {
        return matcher.match(request.headers());
    }

    @Override
    public String toString() {
        return "HeaderCondition{" +
                "matcher=" + matcher +
                '}';
    }
}
