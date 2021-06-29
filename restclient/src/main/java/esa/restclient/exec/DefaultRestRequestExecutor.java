package esa.restclient.exec;

import esa.httpclient.core.HttpClient;
import esa.httpclient.core.util.OrderedComparator;
import esa.restclient.RestClientConfig;
import esa.restclient.codec.BodyProcessor;
import esa.restclient.interceptor.Interceptor;
import esa.restclient.request.RestHttpRequest;
import esa.restclient.response.RestHttpResponse;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class DefaultRestRequestExecutor implements RestRequestExecutor {

    private final InvocationChain invocationChain;

    public DefaultRestRequestExecutor(HttpClient httpClient, RestClientConfig clientConfig, BodyProcessor bodyProcessor) {
        this.invocationChain = buildInvokeChain(httpClient, clientConfig, bodyProcessor);
    }

    @Override
    public CompletionStage<RestHttpResponse> execute(RestHttpRequest request) {
        return invocationChain.proceed(request);
    }

    private InvocationChain buildInvokeChain(HttpClient httpClient, RestClientConfig clientConfig, BodyProcessor bodyProcessor) {
        InvocationChain invocationChain = new RequestInvocation(httpClient, clientConfig, bodyProcessor);
        List<Interceptor> interceptors = clientConfig.interceptors();
        if (interceptors.size() == 0) {
            return invocationChain;
        }
        final List<Interceptor> interceptors0 = new LinkedList<>(clientConfig.interceptors());
        OrderedComparator.sort(interceptors0);
        Interceptor[] orderedInterceptors = Collections.unmodifiableList(interceptors0).toArray(new Interceptor[0]);
        for (int i = orderedInterceptors.length - 1; i >= 0; i--) {
            invocationChain = new InterceptorInvocationChain(orderedInterceptors[i], invocationChain);
        }
        return invocationChain;
    }

}
