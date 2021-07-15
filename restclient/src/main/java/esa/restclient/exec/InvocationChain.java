package esa.restclient.exec;

import esa.restclient.RestRequest;
import esa.restclient.RestResponse;
import esa.restclient.interceptor.Interceptor;

import java.util.concurrent.CompletionStage;

public interface InvocationChain {

    /**
     * Invoke the registered {@link Interceptor}s.
     *
     * @param request current request
     * @return future
     */
    CompletionStage<RestResponse> proceed(RestRequest request);

}
