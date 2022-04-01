package io.esastack.restclient.ext.rule.impl;

import esa.commons.Checks;
import esa.commons.spi.SpiLoader;
import io.esastack.httpclient.core.util.LoggerUtils;
import io.esastack.restclient.ext.action.RequestRedefineActionFactory;
import io.esastack.restclient.ext.condition.RequestRedefineConditionFactory;
import io.esastack.restclient.ext.config.RedefineRuleConfig;
import io.esastack.restclient.ext.config.RedefineRuleConfigsSupplier;
import io.esastack.restclient.ext.rule.RedefineRule;
import io.esastack.restclient.ext.rule.RedefineRuleFactory;
import io.esastack.restclient.ext.rule.RedefineRulesManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RedefineRulesManagerImpl implements RedefineRulesManager {

    private final RedefineRuleFactory ruleFactory;
    private volatile List<RedefineRule> rules;

    public RedefineRulesManagerImpl() {
        List<RedefineRuleConfigsSupplier> configsSuppliers = SpiLoader
                .cached(RedefineRuleConfigsSupplier.class)
                .getAll();
        int suppliersSize = configsSuppliers.size();
        if (suppliersSize == 0 || suppliersSize > 1) {
            throw new IllegalStateException("Unexpected size of ConfigsSuppliers : " + suppliersSize
                    + ", expected size is 1. configsSuppliers: " + configsSuppliers);
        }
        RedefineRuleConfigsSupplier configsSupplier = configsSuppliers.get(0);

        // 通过SPI加载  ConditionFactory 与 ActionFactory，并注册到RuleFactory中去
        this.ruleFactory = new RedefineRuleFactoryImpl();
        List<RequestRedefineConditionFactory> conditionFactories = SpiLoader
                .cached(RequestRedefineConditionFactory.class)
                .getAll();
        for (RequestRedefineConditionFactory conditionFactory : conditionFactories) {
            ruleFactory.registerConditionFactory(conditionFactory);
        }

        List<RequestRedefineActionFactory> actionFactories = SpiLoader
                .cached(RequestRedefineActionFactory.class)
                .getAll();

        for (RequestRedefineActionFactory actionFactory : actionFactories) {
            ruleFactory.registerActionFactory(actionFactory);
        }

        refreshRules(configsSupplier.configs());
        configsSupplier.registerChangeEvent(this::refreshRules);
    }

    @Override
    public List<RedefineRule> rules() {
        if (rules == null) {
            return Collections.emptyList();
        }
        return rules;
    }

    private void refreshRules(Collection<RedefineRuleConfig> ruleConfigs) {
        Checks.checkNotNull(ruleConfigs, "ruleConfigs");
        List<RedefineRule> rules = new ArrayList<>(8);
        for (RedefineRuleConfig config : ruleConfigs) {
            try {
                rules.add(ruleFactory.create(config));
            } catch (Throwable e) {
                LoggerUtils.logger().error("Create redefineRule error!config:{}", config, e);
            }
        }
        this.rules = rules;
    }
}
