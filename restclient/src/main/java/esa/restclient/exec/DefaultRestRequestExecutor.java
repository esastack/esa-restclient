package esa.restclient.exec;

import esa.httpclient.core.util.OrderedComparator;
import esa.restclient.RestClientConfig;
import esa.restclient.RestHttpRequest;
import esa.restclient.RestHttpResponse;
import esa.restclient.interceptor.Interceptor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class DefaultRestRequestExecutor implements RestRequestExecutor {

    private final InvocationChain invocationChain;

    public DefaultRestRequestExecutor(RestClientConfig clientConfig) {
        this.invocationChain = buildInvokeChain(clientConfig);
    }

    @Override
    public CompletionStage<RestHttpResponse> execute(RestHttpRequest request, RequestAction requestAction) {
        return invocationChain.proceed(request, requestAction);
    }

    private InvocationChain buildInvokeChain(RestClientConfig clientConfig) {
        InvocationChain invocationChain =
                (request, requestAction) -> requestAction.doRequest(request);

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
