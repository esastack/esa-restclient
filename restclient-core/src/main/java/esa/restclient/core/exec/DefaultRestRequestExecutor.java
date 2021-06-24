package esa.restclient.core.exec;

import esa.httpclient.core.HttpClient;
import esa.httpclient.core.util.OrderedComparator;
import esa.restclient.core.RestClientConfig;
import esa.restclient.core.codec.BodyProcessor;
import esa.restclient.core.interceptor.Interceptor;
import esa.restclient.core.request.RestHttpRequest;
import esa.restclient.core.response.RestHttpResponse;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class DefaultRestRequestExecutor implements RestRequestExecutor {
    private final InvokeChain invokeChain;

    public DefaultRestRequestExecutor(HttpClient httpClient, RestClientConfig clientConfig, BodyProcessor bodyProcessor) {
        this.invokeChain = buildInvokeChain(httpClient, clientConfig, bodyProcessor);
    }

    @Override
    public CompletionStage<RestHttpResponse> execute(RestHttpRequest request) {
        return invokeChain.proceed(request);
    }

    private InvokeChain buildInvokeChain(HttpClient httpClient, RestClientConfig clientConfig, BodyProcessor bodyProcessor) {
        InvokeChain invokeChain = new RequestInvoke(httpClient, clientConfig, bodyProcessor);
        List<Interceptor> interceptors = clientConfig.interceptors();
        if (interceptors.size() == 0) {
            return invokeChain;
        }
        final List<Interceptor> interceptors0 = new LinkedList<>(clientConfig.interceptors());
        OrderedComparator.sort(interceptors0);
        Interceptor[] orderedInterceptors = Collections.unmodifiableList(interceptors0).toArray(new Interceptor[0]);
        for (int i = orderedInterceptors.length - 1; i >= 0; i--) {
            invokeChain = new InterceptorInvokeChain(orderedInterceptors[i], invokeChain);
        }
        return invokeChain;
    }
}
