package io.esastack.restclient.exec;

import io.esastack.restclient.RequestInvocation;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponseBase;

import java.util.concurrent.CompletionStage;

public class RestRequestExecutorImpl implements RestRequestExecutor {

    private final InvocationChain invocationChain;

    public RestRequestExecutorImpl(RestClientOptions clientOptions) {
        this.invocationChain = buildInvokeChain(clientOptions);
    }

    @Override
    public CompletionStage<RestResponseBase> execute(RestRequest request) {
        return invocationChain.proceed(request)
                .thenApply(response -> (RestResponseBase) response);
    }

    private InvocationChain buildInvokeChain(RestClientOptions clientOptions) {
        InvocationChain invocationChain = new RequestInvocation();

        ClientInterceptor[] orderedInterceptors = clientOptions.unmodifiableInterceptors();

        if (orderedInterceptors.length == 0) {
            return invocationChain;
        }
        for (int i = orderedInterceptors.length - 1; i >= 0; i--) {
            invocationChain = new InvocationChainImpl(orderedInterceptors[i], invocationChain);
        }
        return invocationChain;
    }
}
