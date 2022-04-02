package io.esastack.restclient.ext.action.impl;

import esa.commons.Checks;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.ext.RedefineContext;
import io.esastack.restclient.ext.action.RequestRedefineAction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class HeaderAction implements RequestRedefineAction {

    private final Map<String, String> headersToBeAdd;
    private final List<String> headersToBeRemove;

    public HeaderAction(Map<String, String> headersToBeAdd, List<String> headersToBeRemove) {
        Checks.checkNotNull(headersToBeAdd, "headersToBeAdd");
        Checks.checkNotNull(headersToBeRemove, "headersToBeRemove");
        this.headersToBeAdd = headersToBeAdd;
        this.headersToBeRemove = headersToBeRemove;
    }

    @Override
    public CompletionStage<RestResponse> doAction(RedefineContext context) {
        context.request().addHeaders(headersToBeAdd);
        headersToBeRemove.forEach(name -> context.request().removeHeader(name));
        return context.next();
    }
}
