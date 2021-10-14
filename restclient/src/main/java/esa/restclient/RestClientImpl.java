package esa.restclient;

import esa.commons.Checks;
import esa.httpclient.core.CompositeRequest;
import esa.httpclient.core.HttpClient;
import esa.restclient.exec.RestRequestExecutor;
import esa.restclient.exec.RestRequestExecutorImpl;

public class RestClientImpl implements RestClient {

    private final RestClientOptions clientOptions;
    private final RestRequestExecutor requestExecutor;
    private final HttpClient target;

    RestClientImpl(RestClientOptions clientOptions, HttpClient httpClient) {
        Checks.checkNotNull(clientOptions, "ClientOptions must not be null!");
        Checks.checkNotNull(httpClient, "HttpClient must not be null!");
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
