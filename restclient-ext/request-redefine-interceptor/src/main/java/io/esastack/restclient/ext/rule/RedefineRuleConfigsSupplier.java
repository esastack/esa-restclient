package io.esastack.restclient.ext.rule;


import java.util.Collection;
import java.util.function.Consumer;

public interface RedefineRuleConfigsSupplier {

    boolean isChanged(Collection<RedefineRuleConfig> before);

    Collection<RedefineRuleConfig> configs();

    void registerChangeEvent(Consumer<Collection<RedefineRuleConfig>> consumer);
}
