package io.esastack.restclient.ext.condition;

public interface RequestRedefineConditionFactory {

    String name();

    RequestRedefineCondition create(String config);
}
