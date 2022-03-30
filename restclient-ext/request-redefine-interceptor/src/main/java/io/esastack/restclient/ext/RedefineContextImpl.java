package io.esastack.restclient.ext;

import esa.commons.Checks;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.exec.InvocationChain;
import io.esastack.restclient.ext.action.RequestRedefineAction;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class RedefineContextImpl implements RedefineContext {

    private final RestRequest request;
    private final List<RequestRedefineAction> actions;
    private final int actionsSize;
    private final InvocationChain invocationChain;
    private int actionsIndex;
    private boolean hadProceed = false;

    public RedefineContextImpl(RestRequest request,
                               List<RequestRedefineAction> actions,
                               InvocationChain invocationChain) {
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(actions, "actions");
        Checks.checkNotNull(invocationChain, "invocationChain");
        this.request = request;
        this.actions = actions;
        this.actionsSize = actions.size();
        this.invocationChain = invocationChain;
    }

    @Override
    public RestRequest request() {
        return request;
    }

    @Override
    public CompletionStage<RestResponse> next() {
        if (hadProceed) {
            throw new IllegalStateException("The context had end!Please don,t call next() repeat in the one action!");
        }

        if (actionsIndex == actionsSize) {
            this.hadProceed = true;
            return invocationChain.proceed(request);
        }

        return actions.get(actionsIndex++).doAction(this);
    }
}
