package io.esastack.restclient.ext.condition.impl;

import io.esastack.restclient.ext.condition.RequestRedefineCondition;
import io.esastack.restclient.ext.condition.RequestRedefineConditionFactory;
import io.esastack.restclient.ext.config.ConfigItem;

public class PathConditionFactory implements RequestRedefineConditionFactory {

    private static final String PATH = "path";

    @Override
    public String type() {
        return PATH;
    }

    @Override
    public RequestRedefineCondition create(ConfigItem config) {
        return new PathCondition(config.getContent(StringMatcher.class));
    }
}
