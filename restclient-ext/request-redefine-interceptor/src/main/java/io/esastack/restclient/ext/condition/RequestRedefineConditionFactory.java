package io.esastack.restclient.ext.condition;

import io.esastack.restclient.ext.config.ConfigItem;

public interface RequestRedefineConditionFactory {

    String type();

    RequestRedefineCondition create(ConfigItem config);
}
