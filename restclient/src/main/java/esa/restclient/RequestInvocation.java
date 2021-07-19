package esa.restclient;

import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.util.Futures;
import esa.restclient.exec.InvocationChain;

import java.util.concurrent.CompletionStage;

public class RequestInvocation implements InvocationChain {

    @Override
    public CompletionStage<RestResponse> proceed(RestRequest request) {
        if (!(request instanceof AbstractExecutableRestRequest)) {
            throw new IllegalStateException("The type of the request is not AbstractExecutableRestRequest!" +
                    "RequestType:" + request.getClass() + "," +
                    "Request:" + request);
        }

        final AbstractExecutableRestRequest executableRequest = (AbstractExecutableRestRequest) request;
        try {
            executableRequest.fillBody();
        } catch (Exception e) {
            return Futures.completed(e);
        }

        return executableRequest.target.execute()
                .thenApply((response) -> processResponse(executableRequest, response));
    }

    private RestResponse processResponse(AbstractExecutableRestRequest request, HttpResponse response) {
        return new RestResponseImpl(request, response, request.clientConfig);
    }
}
