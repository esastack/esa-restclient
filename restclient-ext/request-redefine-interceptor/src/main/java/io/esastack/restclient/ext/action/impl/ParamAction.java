package io.esastack.restclient.ext.action.impl;

import esa.commons.Checks;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.ext.RedefineContext;
import io.esastack.restclient.ext.action.RequestRedefineAction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class ParamAction implements RequestRedefineAction {

    private final Map<String, String> paramsToBeAdd;
    private final List<String> paramsToBeRemove;

    public ParamAction(Map<String, String> paramsToBeAdd, List<String> paramsToBeRemove) {
        Checks.checkNotNull(paramsToBeAdd, "paramsToBeAdd");
        Checks.checkNotNull(paramsToBeRemove, "paramsToBeRemove");
        this.paramsToBeAdd = paramsToBeAdd;
        this.paramsToBeRemove = paramsToBeRemove;
    }

    @Override
    public CompletionStage<RestResponse> doAction(RedefineContext context) {
        context.request().addParams(paramsToBeAdd);
        paramsToBeRemove.forEach(name -> context.request().uri().params().remove(name));
        return context.next();
    }
}
