package esa.restclient.core.interceptor;


import esa.restclient.core.request.HttpRequest;
import esa.restclient.core.response.HttpResponse;

import java.util.concurrent.CompletionStage;

public interface InvokeChain {

    /**
     * Invoke the registered {@link Interceptor}s.
     *
     * @param request  current request
     * @return future
     */
    CompletionStage<HttpResponse> proceed(HttpRequest request);
}
