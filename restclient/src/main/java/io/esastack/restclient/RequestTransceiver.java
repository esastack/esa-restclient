package io.esastack.restclient;

import io.esastack.httpclient.core.HttpResponse;
import io.esastack.restclient.exec.InvocationChain;

import java.util.concurrent.CompletionStage;

public final class RequestTransceiver implements InvocationChain {

    private final ClientInnerComposition clientInnerComposition;

    public RequestTransceiver(ClientInnerComposition clientInnerComposition) {
        this.clientInnerComposition = clientInnerComposition;
    }

    @Override
    public CompletionStage<RestResponse> proceed(RestRequest request) {
        if (!(request instanceof AbstractExecutableRestRequest)) {
            throw new IllegalStateException("The type of the request is not AbstractExecutableRestRequest!" +
                    "RequestType:" + request.getClass() + "," +
                    "Request:" + request);
        }

        final AbstractExecutableRestRequest executableRequest = (AbstractExecutableRestRequest) request;

        return executableRequest.sendRequest()
                .thenApply((response) -> processResponse(executableRequest, response));
    }

    private RestResponse processResponse(RestRequestBase request, HttpResponse response) {
        return new RestResponseBaseImpl(request, response, clientInnerComposition);
    }
}
