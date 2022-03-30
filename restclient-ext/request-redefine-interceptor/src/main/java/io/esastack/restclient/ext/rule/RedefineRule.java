package io.esastack.restclient.ext.rule;

import io.esastack.restclient.ext.action.RequestRedefineAction;
import io.esastack.restclient.ext.condition.RequestRedefineCondition;

import java.util.List;

public interface RedefineRule {

    String name();

    List<RequestRedefineCondition> conditions();

    List<RequestRedefineAction> actions();
}
