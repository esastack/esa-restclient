package esa.restclient.core.exec;

import esa.restclient.core.interceptor.Interceptor;
import esa.restclient.core.request.RestHttpRequest;
import esa.restclient.core.response.RestHttpResponse;

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
