package io.esastack.restclient.ext.action;

import io.esastack.restclient.ext.config.ConfigItem;

public interface RequestRedefineActionFactory {

    String type();

    RequestRedefineAction create(ConfigItem config);
}
