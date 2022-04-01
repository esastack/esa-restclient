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

    class MatchResultImpl implements MatchResult {

        private final boolean isMatch;
        private final String unMatchReason;
        private static final MatchResult SUCCESS_MATCH_RESULT = new MatchResultImpl(true, null);

        private MatchResultImpl(boolean isMatch, String unMatchReason) {
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
}
