package io.esastack.restclient;

import esa.commons.Checks;
import io.esastack.httpclient.core.CompositeRequest;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.restclient.exec.RestRequestExecutor;
import io.esastack.restclient.exec.RestRequestExecutorImpl;

public class RestClientImpl implements RestClient {

    private final RestClientOptions clientOptions;
    private final RestRequestExecutor requestExecutor;
    private final HttpClient target;

    RestClientImpl(RestClientOptions clientOptions, HttpClient httpClient) {
        Checks.checkNotNull(clientOptions, "clientOptions");
        Checks.checkNotNull(httpClient, "httpClient");
        this.clientOptions = clientOptions;
        this.requestExecutor = new RestRequestExecutorImpl(clientOptions);
        this.target = httpClient;
    }

    @Override
    public ExecutableRestRequest get(String uri) {
        return new RestCompositeRequest((CompositeRequest) target.get(uri), clientOptions, requestExecutor);
    }

    @Override
    public RestRequestFacade post(String uri) {
        return new RestCompositeRequest((CompositeRequest) target.post(uri), clientOptions, requestExecutor);
    }

    @Override
    public RestRequestFacade delete(String uri) {
        return new RestCompositeRequest((CompositeRequest) target.delete(uri), clientOptions, requestExecutor);
    }

    @Override
    public RestRequestFacade put(String uri) {
        return new RestCompositeRequest((CompositeRequest) target.put(uri), clientOptions, requestExecutor);
    }

    @Override
    public ExecutableRestRequest head(String uri) {
        return new RestCompositeRequest((CompositeRequest) target.head(uri), clientOptions, requestExecutor);
    }

    @Override
    public ExecutableRestRequest options(String uri) {
        return new RestCompositeRequest((CompositeRequest) target.options(uri), clientOptions, requestExecutor);
    }

    @Override
    public RestClientOptions clientOptions() {
        return clientOptions;
    }

}
