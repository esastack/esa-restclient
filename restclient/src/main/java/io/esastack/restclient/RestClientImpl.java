package io.esastack.restclient;

import esa.commons.Checks;
import io.esastack.httpclient.core.CompositeRequest;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.restclient.exec.RestRequestExecutor;
import io.esastack.restclient.exec.RestRequestExecutorImpl;

public class RestClientImpl implements RestClient {

    private final RestRequestExecutor requestExecutor;
    private final HttpClient httpClient;
    private final RestClientOptions clientOptions;

    RestClientImpl(RestClientOptions clientOptions, HttpClient httpClient) {
        Checks.checkNotNull(clientOptions, "clientOptions");
        Checks.checkNotNull(httpClient, "httpClient");
        this.clientOptions = clientOptions;
        this.httpClient = httpClient;
        this.requestExecutor = new RestRequestExecutorImpl(clientOptions);
    }

    @Override
    public ExecutableRestRequest get(String uri) {
        return new RestCompositeRequest((CompositeRequest) httpClient.get(uri),
                clientOptions, requestExecutor);
    }

    @Override
    public RestRequestFacade post(String uri) {
        return new RestCompositeRequest((CompositeRequest) httpClient.post(uri),
                clientOptions, requestExecutor);
    }

    @Override
    public RestRequestFacade delete(String uri) {
        return new RestCompositeRequest((CompositeRequest) httpClient.delete(uri),
                clientOptions, requestExecutor);
    }

    @Override
    public RestRequestFacade put(String uri) {
        return new RestCompositeRequest((CompositeRequest) httpClient.put(uri),
                clientOptions, requestExecutor);
    }

    @Override
    public ExecutableRestRequest head(String uri) {
        return new RestCompositeRequest((CompositeRequest) httpClient.head(uri),
                clientOptions, requestExecutor);
    }

    @Override
    public ExecutableRestRequest options(String uri) {
        return new RestCompositeRequest((CompositeRequest) httpClient
                .options(uri), clientOptions, requestExecutor);
    }

    @Override
    public RestClientOptions clientOptions() {
        return clientOptions;
    }

}
