package io.esastack.restclient.ext.rule;

import io.esastack.restclient.ext.action.RequestRedefineActionFactory;
import io.esastack.restclient.ext.condition.RequestRedefineConditionFactory;

public interface RedefineRuleFactory {

    void registerConditionFactory(RequestRedefineConditionFactory factory);

    void registerActionFactory(RequestRedefineActionFactory factory);

    RedefineRule create(RedefineRuleConfig config);
}
