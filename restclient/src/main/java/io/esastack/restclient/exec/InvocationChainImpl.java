package io.esastack.restclient.exec;

import esa.commons.Checks;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;

import java.util.concurrent.CompletionStage;

class InvocationChainImpl implements InvocationChain {

    private final ClientInterceptor current;
    private final InvocationChain next;

    InvocationChainImpl(ClientInterceptor current, InvocationChain next) {
        Checks.checkNotNull(current, "current");
        Checks.checkNotNull(next, "next");
        this.current = current;
        this.next = next;
    }

    @Override
    public CompletionStage<RestResponse> proceed(RestRequest request) {
        return current.proceed(request, next);
    }
}