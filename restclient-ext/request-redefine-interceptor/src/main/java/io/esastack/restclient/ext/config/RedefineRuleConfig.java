package io.esastack.restclient.ext.config;

import io.esastack.restclient.ext.rule.RedefineRule;

import java.util.List;

public interface RedefineRuleConfig {

    String name();

    RedefineRule.MatchMechanism matchMechanism();

    List<ConfigItem> conditionConfigs();

    List<ConfigItem> actionConfigs();
}
