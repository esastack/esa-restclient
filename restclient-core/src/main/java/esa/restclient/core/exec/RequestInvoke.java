package esa.restclient.core.exec;

import esa.commons.Checks;
import esa.httpclient.core.HttpClient;
import esa.restclient.core.RestClientConfig;
import esa.restclient.core.request.RestHttpRequest;
import esa.restclient.core.response.RestHttpResponse;

import java.util.concurrent.CompletionStage;

public class RequestInvoke implements InvokeChain {
    private final HttpClient httpClient;
    private final RestClientConfig clientConfig;

    public RequestInvoke(HttpClient httpClient, RestClientConfig clientConfig) {
        Checks.checkNotNull(httpClient, "HttpClient must not be null");
        Checks.checkNotNull(clientConfig, "ClientConfig must not be null");
        this.httpClient = httpClient;
        this.clientConfig = clientConfig;
    }


    @Override
    public CompletionStage<RestHttpResponse> proceed(RestHttpRequest request) {

        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }
}
