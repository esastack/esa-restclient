package io.esastack.restclient.ext.condition.impl;

import io.esastack.restclient.ext.condition.RequestRedefineCondition;
import io.esastack.restclient.ext.condition.RequestRedefineConditionFactory;
import io.esastack.restclient.ext.config.ConfigItem;

public class HostConditionFactory implements RequestRedefineConditionFactory {

    private static final String HOST = "host";

    @Override
    public String type() {
        return HOST;
    }

    @Override
    public RequestRedefineCondition create(ConfigItem config) {
        return new HostCondition(config.getContent(StringMatcher.class));
    }
}
