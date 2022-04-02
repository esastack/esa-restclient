package io.esastack.restclient.ext.condition.impl;

import esa.commons.Checks;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.ext.condition.MatchResult;
import io.esastack.restclient.ext.condition.RequestRedefineCondition;

public class MethodCondition implements RequestRedefineCondition {

    private final String method;

    public MethodCondition(String method) {
        Checks.checkNotEmptyArg(method, "Method should not be empty!");
        this.method = method;
    }

    @Override
    public MatchResult match(RestRequest request) {
        String actualMethod = request.method().toString();
        if (method.equalsIgnoreCase(actualMethod)) {
            return MatchResult.success();
        } else {
            return MatchResult.fail("Method don't match," +
                    " expect : " + method + ",actual : " + actualMethod + ".");
        }
    }

    @Override
    public String toString() {
        return "MethodCondition{" +
                "method='" + method + '\'' +
                '}';
    }
}
