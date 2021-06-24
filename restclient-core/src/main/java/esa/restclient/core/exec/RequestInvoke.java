package esa.restclient.core.exec;

import esa.commons.Checks;
import esa.httpclient.core.HttpClient;
import esa.restclient.core.RestClientConfig;
import esa.restclient.core.codec.CodecManager;
import esa.restclient.core.request.RestHttpRequest;
import esa.restclient.core.response.RestHttpResponse;

import java.util.concurrent.CompletionStage;

public class RequestInvoke implements InvokeChain {
    private final HttpClient httpClient;
    private final RestClientConfig clientConfig;
    private final CodecManager codecManager;

    RequestInvoke(HttpClient httpClient, RestClientConfig clientConfig, CodecManager codecManager) {
        Checks.checkNotNull(httpClient, "HttpClient must not be null");
        Checks.checkNotNull(clientConfig, "ClientConfig must not be null");
        Checks.checkNotNull(codecManager, "CodecManager must not be null");
        this.httpClient = httpClient;
        this.clientConfig = clientConfig;
        this.codecManager = codecManager;
    }


    @Override
    public CompletionStage<RestHttpResponse> proceed(RestHttpRequest request) {
        
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }
}
