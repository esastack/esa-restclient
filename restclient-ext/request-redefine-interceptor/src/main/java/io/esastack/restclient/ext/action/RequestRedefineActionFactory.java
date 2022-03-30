package io.esastack.restclient.ext.action;

public interface RequestRedefineActionFactory {

    String name();

    RequestRedefineAction create(String config);
}
