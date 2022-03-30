package io.esastack.restclient.ext.condition;

public interface MatchResult {

    boolean isMatch();

    String unMatchReason();
}
