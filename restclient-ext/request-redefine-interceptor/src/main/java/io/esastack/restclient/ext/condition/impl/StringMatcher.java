package io.esastack.restclient.ext.condition.impl;

import io.esastack.restclient.ext.condition.MatchResult;

public class StringMatcher {
    private String exact;
    private String prefix;
    private String regex;

    public StringMatcher() {
    }

    public MatchResult match(String actual) {
        if (actual == null) {
            actual = "";
        }

        if (exact != null) {
            if (exact.equals(actual)) {
                return MatchResult.success();
            }
        }
        if (prefix != null) {
            if (actual.startsWith(prefix)) {
                return MatchResult.success();
            }
        }

        if (regex != null) {
            if (actual.matches(regex)) {
                return MatchResult.success();
            }
        }
        return MatchResult.fail("Actual(" + actual + ") don't meet expectations("
                + "exact='" + exact + '\'' +
                ", prefix='" + prefix + '\'' +
                ", regex='" + regex + '\'' + ")!");
    }

    public void setExact(String exact) {
        this.exact = exact;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public String toString() {
        return "StringMatcher{" +
                "exact='" + exact + '\'' +
                ", prefix='" + prefix + '\'' +
                ", regex='" + regex + '\'' +
                '}';
    }
}
