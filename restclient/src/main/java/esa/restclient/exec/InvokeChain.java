package esa.restclient.exec;

import esa.restclient.interceptor.Interceptor;
import esa.restclient.request.RestHttpRequest;
import esa.restclient.response.RestHttpResponse;

import java.util.concurrent.CompletionStage;

public interface InvokeChain {

    /**
     * Invoke the registered {@link Interceptor}s.
     *
     * @param request  current request
     * @return future
     */
    CompletionStage<RestHttpResponse> proceed(RestHttpRequest request);
}
