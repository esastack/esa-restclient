package io.esastack.restclient.ext.rule.impl;

import esa.commons.Checks;
import io.esastack.restclient.ext.action.RequestRedefineAction;
import io.esastack.restclient.ext.action.RequestRedefineActionFactory;
import io.esastack.restclient.ext.condition.RequestRedefineCondition;
import io.esastack.restclient.ext.condition.RequestRedefineConditionFactory;
import io.esastack.restclient.ext.config.ConfigItem;
import io.esastack.restclient.ext.config.RedefineRuleConfig;
import io.esastack.restclient.ext.rule.RedefineRule;
import io.esastack.restclient.ext.rule.RedefineRuleFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedefineRuleFactoryImpl implements RedefineRuleFactory {

    private final Map<String, RequestRedefineConditionFactory> conditionFactoryMap = new HashMap<>();
    private final Map<String, RequestRedefineActionFactory> actionFactoryMap = new HashMap<>();

    @Override
    public void registerConditionFactory(RequestRedefineConditionFactory factory) {
        Checks.checkNotNull(factory, "conditionFactory");
        conditionFactoryMap.put(factory.type(), factory);
    }

    @Override
    public void registerActionFactory(RequestRedefineActionFactory factory) {
        Checks.checkNotNull(factory, "actionFactory");
        actionFactoryMap.put(factory.type(), factory);
    }

    @Override
    public RedefineRule create(RedefineRuleConfig config) {
        Checks.checkNotNull(config, "config");
        List<RequestRedefineCondition> conditions = new ArrayList<>(3);
        List<RequestRedefineAction> actions = new ArrayList<>(3);
        List<ConfigItem> conditionConfigs = config.conditionConfigs();
        if (conditionConfigs != null) {
            for (ConfigItem configItem : conditionConfigs) {
                String type = configItem.type();
                RequestRedefineConditionFactory conditionFactory = conditionFactoryMap.get(type);
                if (conditionFactory == null) {
                    throw new IllegalStateException("There is no conditionFactory for condition: " + type);
                }
                conditions.add(conditionFactory.create(configItem));
            }
        }

        List<ConfigItem> actionConfigs = config.actionConfigs();
        if (actionConfigs != null) {
            for (ConfigItem configItem : actionConfigs) {
                String type = configItem.type();
                RequestRedefineActionFactory actionFactory = actionFactoryMap.get(type);
                if (actionFactory == null) {
                    throw new IllegalStateException("There is no actionFactory for action: " + type);
                }
                actions.add(actionFactory.create(configItem));
            }
        }
        return new RedefineRuleImpl(config.name(), conditions, actions);
    }
}
