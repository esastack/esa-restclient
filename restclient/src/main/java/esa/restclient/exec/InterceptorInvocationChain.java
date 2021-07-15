package esa.restclient.exec;

import esa.commons.Checks;
import esa.restclient.RestRequest;
import esa.restclient.RestResponse;
import esa.restclient.interceptor.Interceptor;

import java.util.concurrent.CompletionStage;

public class InterceptorInvocationChain implements InvocationChain {

    private final Interceptor current;
    private final InvocationChain next;

    public InterceptorInvocationChain(Interceptor current, InvocationChain next) {
        Checks.checkNotNull(current, "Current must not be null");
        Checks.checkNotNull(next, "Next must not be null");
        this.current = current;
        this.next = next;
    }

    @Override
    public CompletionStage<RestResponse> proceed(RestRequest request) {
        return current.proceed(request, next);
    }
}
