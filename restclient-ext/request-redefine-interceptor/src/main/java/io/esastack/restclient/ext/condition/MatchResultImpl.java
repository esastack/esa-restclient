package io.esastack.restclient.ext.condition;

class MatchResultImpl implements MatchResult {

    private final boolean isMatch;
    private final String unMatchReason;
    static final MatchResult SUCCESS_MATCH_RESULT = new MatchResultImpl(true, null);

    MatchResultImpl(boolean isMatch, String unMatchReason) {
        this.isMatch = isMatch;
        this.unMatchReason = unMatchReason;
    }

    @Override
    public boolean isMatch() {
        return isMatch;
    }

    @Override
    public String unMatchReason() {
        return unMatchReason;
    }
}
