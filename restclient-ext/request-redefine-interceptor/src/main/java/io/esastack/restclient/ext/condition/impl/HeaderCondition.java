package io.esastack.restclient.ext.condition.impl;

import io.esastack.restclient.RestRequest;
import io.esastack.restclient.ext.condition.MatchResult;
import io.esastack.restclient.ext.condition.RequestRedefineCondition;

public class HeaderCondition implements RequestRedefineCondition {

    private final HeaderMatcher matcher;

    public HeaderCondition(HeaderMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public MatchResult match(RestRequest request) {
        return matcher.match(request.headers());
    }
}
