package io.esastack.restclient.exec;

import io.esastack.httpclient.core.util.Ordered;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;

import java.util.concurrent.CompletionStage;

/**
 * Interceptor is designed for intercept request execution.Users can execute business
 * logic or modify the parameters of the request before and after executing the request.
 */
public interface ClientInterceptor extends Ordered {

    /**
     * Proceeds the RestRequest and obtains RestResponse.Users can execute business
     * logic or modify the parameters of the request before and after executing the
     * request.
     *
     * @param request request
     * @param next    next,{@link InvocationChain#proceed} will continue execute the
     *               execution chain
     * @return response
     */
    CompletionStage<RestResponse> proceed(RestRequest request, InvocationChain next);

}
