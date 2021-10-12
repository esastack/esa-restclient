package esa.restclient.exec;

import esa.httpclient.core.util.Ordered;
import esa.restclient.RestRequest;
import esa.restclient.RestResponse;

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
