package io.esastack.restclient.ext.rule.impl;

import esa.commons.Checks;
import io.esastack.restclient.ext.action.RequestRedefineAction;
import io.esastack.restclient.ext.condition.RequestRedefineCondition;
import io.esastack.restclient.ext.rule.RedefineRule;

import java.util.List;

public class RedefineRuleImpl implements RedefineRule {
    private final String name;
    private final List<RequestRedefineCondition> conditions;
    private final List<RequestRedefineAction> actions;

    public RedefineRuleImpl(String name,
                            List<RequestRedefineCondition> conditions,
                            List<RequestRedefineAction> actions) {
        Checks.checkNotNull(name, "name");
        Checks.checkNotNull(conditions, "conditions");
        Checks.checkNotNull(actions, "actions");
        this.name = name;
        this.conditions = conditions;
        this.actions = actions;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<RequestRedefineCondition> conditions() {
        return conditions;
    }

    @Override
    public List<RequestRedefineAction> actions() {
        return actions;
    }
}
