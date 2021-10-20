package io.esastack.restclient.exec;

import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;

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
