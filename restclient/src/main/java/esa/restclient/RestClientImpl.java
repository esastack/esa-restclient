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
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public RestRequestFacade post(String uri) {
        return new RestCompositeRequest((CompositeRequest) target.post(uri), clientOptions, requestExecutor);
    }

    @Override
    public RestRequestFacade delete(String uri) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public RestRequestFacade put(String uri) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public ExecutableRestRequest head(String uri) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public ExecutableRestRequest options(String uri) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public RestClientOptions clientOptions() {
        return clientOptions;
    }

}
