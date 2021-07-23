package esa.restclient.exec;

import esa.httpclient.core.util.OrderedComparator;
import esa.restclient.RequestInvocation;
import esa.restclient.RestClientConfig;
import esa.restclient.RestRequest;
import esa.restclient.RestResponseBase;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class RestRequestExecutorImpl implements RestRequestExecutor {

    private final InvocationChain invocationChain;

    public RestRequestExecutorImpl(RestClientConfig clientConfig) {
        this.invocationChain = buildInvokeChain(clientConfig);
    }

    @Override
    public CompletionStage<RestResponseBase> execute(RestRequest request) {
        return invocationChain.proceed(request)
                .thenApply(response -> (RestResponseBase) response);
    }

    private InvocationChain buildInvokeChain(RestClientConfig clientConfig) {
        InvocationChain invocationChain = new RequestInvocation();

        List<Interceptor> interceptors = clientConfig.interceptors();

        if (interceptors.size() == 0) {
            return invocationChain;
        }
        final List<Interceptor> interceptors0 = new LinkedList<>(interceptors);
        OrderedComparator.sort(interceptors0);
        for (int i = interceptors0.size() - 1; i >= 0; i--) {
            invocationChain = new InterceptorInvocationChain(interceptors0.get(i), invocationChain);
        }
        return invocationChain;
    }
}
