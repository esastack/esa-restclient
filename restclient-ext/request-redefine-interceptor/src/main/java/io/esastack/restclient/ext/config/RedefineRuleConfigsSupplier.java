package io.esastack.restclient.ext.config;


import java.util.Collection;
import java.util.function.Consumer;

public interface RedefineRuleConfigsSupplier {

    Collection<RedefineRuleConfig> configs();

    void registerChangeEvent(Consumer<Collection<RedefineRuleConfig>> consumer);
}
