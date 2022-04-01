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
    private final MatchMechanism matchMechanism;

    public RedefineRuleImpl(String name,
                            List<RequestRedefineCondition> conditions,
                            List<RequestRedefineAction> actions,
                            MatchMechanism matchMechanism) {
        Checks.checkNotNull(name, "name");
        Checks.checkNotNull(conditions, "conditions");
        Checks.checkNotNull(actions, "actions");
        Checks.checkNotNull(matchMechanism, "matchMechanism");
        this.name = name;
        this.conditions = conditions;
        this.actions = actions;
        this.matchMechanism = matchMechanism;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public MatchMechanism matchMechanism() {
        return matchMechanism;
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
