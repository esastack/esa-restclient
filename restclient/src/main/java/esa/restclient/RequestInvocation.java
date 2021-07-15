package esa.restclient;

import esa.commons.http.HttpHeaderNames;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.util.Futures;
import esa.restclient.exec.InvocationChain;
import esa.restclient.serializer.TxSerializer;

import java.util.concurrent.CompletionStage;

public class RequestInvocation implements InvocationChain {

    @Override
    public CompletionStage<RestResponse> proceed(RestRequest request) {
        if (!(request instanceof AbstractExecutableRestRequest)) {
            throw new IllegalStateException("Request is not of the expected type!" +
                    "RequestType:" + request.getClass() + "," +
                    "Request:" + request);
        }
        AbstractExecutableRestRequest executableRequest = (AbstractExecutableRestRequest) request;
        try {
            ContentType contentType = executableRequest.computeContentType();
            if (contentType == null) {
                throw new IllegalStateException("The request has no contentType," +
                        "Please set the correct contentType or contentTypeFactory");
            }
            executableRequest.target.setHeader(HttpHeaderNames.CONTENT_TYPE, contentType.getMediaType().toString());

            TxSerializer txSerializer = contentType.txSerializer();
            if (txSerializer != ContentType.NO_SERIALIZE) {
                executableRequest.target.body(txSerializer.serialize(executableRequest.body()));
            }
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
