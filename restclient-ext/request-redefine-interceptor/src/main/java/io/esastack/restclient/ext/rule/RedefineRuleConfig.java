package io.esastack.restclient.ext.rule;

import java.util.Map;

public interface RedefineRuleConfig {

    Map<String, String> conditionConfigMap();

    Map<String, String> actionConfigMap();
}
