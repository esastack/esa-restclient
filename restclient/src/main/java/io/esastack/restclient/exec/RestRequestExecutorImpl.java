package io.esastack.restclient.exec;

import io.esastack.restclient.RequestTransceiver;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponseBase;

import java.util.List;
import java.util.concurrent.CompletionStage;

public final class RestRequestExecutorImpl implements RestRequestExecutor {

    private final InvocationChain invocationChain;

    public RestRequestExecutorImpl(RestClientOptions clientOptions) {
        this.invocationChain = buildInvokeChain(clientOptions.unmodifiableInterceptors(),
                clientOptions);
    }

    @Override
    public CompletionStage<RestResponseBase> execute(RestRequest request) {
        return invocationChain.proceed(request)
                .thenApply(response -> (RestResponseBase) response);
    }

    private InvocationChain buildInvokeChain(List<ClientInterceptor> orderedInterceptors,
                                             RestClientOptions clientOptions) {
        InvocationChain invocationChain = new RequestTransceiver(clientOptions);
        int size = orderedInterceptors.size();
        if (size == 0) {
            return invocationChain;
        }
        for (int i = size - 1; i >= 0; i--) {
            invocationChain = new InvocationChainImpl(orderedInterceptors.get(i), invocationChain);
        }
        return invocationChain;
    }
}
