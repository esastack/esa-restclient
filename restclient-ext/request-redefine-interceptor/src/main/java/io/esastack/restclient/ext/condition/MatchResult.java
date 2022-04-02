package io.esastack.restclient.ext.condition;

public interface MatchResult {

    boolean isMatch();

    String unMatchReason();

    static MatchResult success() {
        return MatchResultImpl.SUCCESS_MATCH_RESULT;
    }

    static MatchResult fail(String reason) {
        return new MatchResultImpl(false, reason);
    }

}
