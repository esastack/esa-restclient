package io.esastack.restclient.ext.condition.impl;

import io.esastack.restclient.ext.condition.RequestRedefineCondition;
import io.esastack.restclient.ext.condition.RequestRedefineConditionFactory;
import io.esastack.restclient.ext.config.ConfigItem;

public class HeaderConditionFactory implements RequestRedefineConditionFactory {

    private static final String HEADER = "header";

    @Override
    public String type() {
        return HEADER;
    }

    @Override
    public RequestRedefineCondition create(ConfigItem config) {
        return new HeaderCondition(config.getContent(HeaderMatcher.class));
    }
}
