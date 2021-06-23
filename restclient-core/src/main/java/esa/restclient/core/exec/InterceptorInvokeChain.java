package esa.restclient.core.exec;

import esa.commons.Checks;
import esa.restclient.core.interceptor.Interceptor;
import esa.restclient.core.request.RestHttpRequest;
import esa.restclient.core.response.RestHttpResponse;

import java.util.concurrent.CompletionStage;

public class InterceptorInvokeChain implements InvokeChain {
    private final Interceptor current;
    private final InvokeChain next;

    public InterceptorInvokeChain(Interceptor current, InvokeChain next) {
        Checks.checkNotNull(current, "Current must not be null");
        Checks.checkNotNull(next, "Next must not be null");
        this.current = current;
        this.next = next;
    }

    @Override
    public CompletionStage<RestHttpResponse> proceed(RestHttpRequest request) {
        return current.proceed(request, next);
    }
}
