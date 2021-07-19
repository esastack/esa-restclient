package esa.restclient;

import esa.commons.http.HttpHeaderNames;
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
            ContentType contentType = executableRequest.computeContentType();
            executableRequest.target.setHeader(HttpHeaderNames.CONTENT_TYPE, contentType.getMediaType().toString());
            executableRequest.fillBody(contentType);
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
