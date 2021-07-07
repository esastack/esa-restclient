package esa.restclient.exec;

import esa.restclient.interceptor.Interceptor;
import esa.restclient.RestHttpRequest;
import esa.restclient.RestHttpResponse;

import java.util.concurrent.CompletionStage;

public interface InvocationChain {

    /**
     * Invoke the registered {@link Interceptor}s.
     *
     * @param request  current request
     * @return future
     */
    CompletionStage<RestHttpResponse> proceed(RestHttpRequest request,RequestAction requestAction);

}
