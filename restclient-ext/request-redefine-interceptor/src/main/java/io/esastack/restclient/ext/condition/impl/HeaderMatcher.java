package io.esastack.restclient.ext.condition.impl;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.restclient.ext.condition.MatchResult;

public class HeaderMatcher {
    private String contains;
    private String name;
    private StringMatcher value;

    public HeaderMatcher() {
    }

    public MatchResult match(HttpHeaders headers) {
        if (contains != null) {
            if (headers.contains(contains)) {
                return MatchResult.success();
            }
        }
        if (name != null) {
            String headerValue = headers.get(name);
            if (headerValue != null) {
                return value.match(headerValue);
            }
        }

        return MatchResult.fail("Header don't meet expectations("
                + "contains='" + contains + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' + ")!");
    }

    public void setContains(String contains) {
        this.contains = contains;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(StringMatcher value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "HeaderMatcher{" +
                "contains='" + contains + '\'' +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
