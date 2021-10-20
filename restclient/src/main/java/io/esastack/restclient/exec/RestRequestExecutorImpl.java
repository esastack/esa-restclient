package io.esastack.restclient.exec;

import io.esastack.httpclient.core.util.OrderedComparator;
import io.esastack.restclient.RequestInvocation;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponseBase;

import java.util.LinkedList;
import java.util.List;
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

        List<ClientInterceptor> interceptors = clientOptions.interceptors();

        if (interceptors.size() == 0) {
            return invocationChain;
        }
        final List<ClientInterceptor> interceptors0 = new LinkedList<>(interceptors);
        OrderedComparator.sort(interceptors0);
        for (int i = interceptors0.size() - 1; i >= 0; i--) {
            invocationChain = new InvocationChainImpl(interceptors0.get(i), invocationChain);
        }
        return invocationChain;
    }
}
