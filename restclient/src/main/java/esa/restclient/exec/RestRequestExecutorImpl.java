package esa.restclient.exec;

import esa.httpclient.core.util.OrderedComparator;
import esa.restclient.*;
import esa.restclient.interceptor.Interceptor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class RestRequestExecutorImpl implements RestRequestExecutor {

    private final InvocationChain invocationChain;

    public RestRequestExecutorImpl(RestClientConfig clientConfig) {
        this.invocationChain = buildInvokeChain(clientConfig);
    }

    @Override
    public CompletionStage<RestResponse> execute(RestRequest request) {
        return invocationChain.proceed(request);
    }

    private InvocationChain buildInvokeChain(RestClientConfig clientConfig) {
        InvocationChain invocationChain = new RequestInvocation();

        List<Interceptor> interceptors = clientConfig.interceptors();

        if (interceptors.size() == 0) {
            return invocationChain;
        }
        final List<Interceptor> interceptors0 = new LinkedList<>(interceptors);
        OrderedComparator.sort(interceptors0);
        Interceptor[] orderedInterceptors = Collections.unmodifiableList(interceptors0).toArray(new Interceptor[0]);
        for (int i = orderedInterceptors.length - 1; i >= 0; i--) {
            invocationChain = new InterceptorInvocationChain(orderedInterceptors[i], invocationChain);
        }
        return invocationChain;
    }
}
