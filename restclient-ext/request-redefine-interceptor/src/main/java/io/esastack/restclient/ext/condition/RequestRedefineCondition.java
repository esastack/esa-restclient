package io.esastack.restclient.ext.condition;

import io.esastack.restclient.RestRequest;

public interface RequestRedefineCondition {

    MatchResult match(RestRequest request);
}
