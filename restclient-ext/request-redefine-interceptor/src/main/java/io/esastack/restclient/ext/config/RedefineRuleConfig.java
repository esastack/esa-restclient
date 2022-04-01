package io.esastack.restclient.ext.config;

import java.util.List;

public interface RedefineRuleConfig {

    String name();

    List<ConfigItem> conditionConfigs();

    List<ConfigItem> actionConfigs();
}
