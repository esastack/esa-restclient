package io.esastack.restclient.ext.condition.impl;

import io.esastack.restclient.RestRequest;
import io.esastack.restclient.ext.condition.MatchResult;

public class ParamMatcher {
    private String contains;
    private String name;
    private StringMatcher value;

    public ParamMatcher() {
    }

    public MatchResult match(RestRequest request) {
        if (contains != null) {
            if (request.paramNames().contains(contains)) {
                return MatchResult.success();
            }
        }
        if (name != null) {
            String paramValue = request.getParam(name);
            if (paramValue != null) {
                return value.match(paramValue);
            }
        }

        return MatchResult.fail("Param don't meet expectations("
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
        return "ParamMatcher{" +
                "contains='" + contains + '\'' +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
