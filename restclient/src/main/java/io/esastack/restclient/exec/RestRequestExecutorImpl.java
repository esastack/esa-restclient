package io.esastack.restclient.exec;

import io.esastack.restclient.ClientInnerComposition;
import io.esastack.restclient.RequestTransceiver;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponseBase;

import java.util.concurrent.CompletionStage;

public final class RestRequestExecutorImpl implements RestRequestExecutor {

    private final InvocationChain invocationChain;

    public RestRequestExecutorImpl(ClientInnerComposition clientInnerComposition) {
        this.invocationChain = buildInvokeChain(clientInnerComposition.interceptors(),
                clientInnerComposition);
    }

    @Override
    public CompletionStage<RestResponseBase> execute(RestRequest request) {
        return invocationChain.proceed(request)
                .thenApply(response -> (RestResponseBase) response);
    }

    private InvocationChain buildInvokeChain(ClientInterceptor[] orderedInterceptors,
                                             ClientInnerComposition clientInnerComposition) {
        InvocationChain invocationChain = new RequestTransceiver(clientInnerComposition);
        if (orderedInterceptors.length == 0) {
            return invocationChain;
        }
        for (int i = orderedInterceptors.length - 1; i >= 0; i--) {
            invocationChain = new InvocationChainImpl(orderedInterceptors[i], invocationChain);
        }
        return invocationChain;
    }
}
