package io.esastack.restclient.ext.condition.impl;

import io.esastack.restclient.ext.condition.RequestRedefineCondition;
import io.esastack.restclient.ext.condition.RequestRedefineConditionFactory;
import io.esastack.restclient.ext.config.ConfigItem;

public class MethodConditionFactory implements RequestRedefineConditionFactory {

    private static final String METHOD = "method";

    @Override
    public String type() {
        return METHOD;
    }

    @Override
    public RequestRedefineCondition create(ConfigItem config) {
        return new MethodCondition(config.getContent(String.class));
    }
}
