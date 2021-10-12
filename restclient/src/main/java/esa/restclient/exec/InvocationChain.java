package esa.restclient.exec;

import esa.restclient.RestRequest;
import esa.restclient.RestResponse;

import java.util.concurrent.CompletionStage;

public interface InvocationChain {

    /**
     * Invoke the registered {@link ClientInterceptor}s.
     *
     * @param request current request
     * @return future
     */
    CompletionStage<RestResponse> proceed(RestRequest request);

}
