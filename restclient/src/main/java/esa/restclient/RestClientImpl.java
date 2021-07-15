package esa.restclient;

import esa.commons.Checks;
import esa.httpclient.core.CompositeRequest;
import esa.httpclient.core.HttpClient;
import esa.restclient.exec.RestRequestExecutorImpl;
import esa.restclient.exec.RestRequestExecutor;

public class RestClientImpl implements RestClient {

    private final RestClientConfig clientConfig;
    private final RestRequestExecutor requestExecutor;
    private final HttpClient target;

    RestClientImpl(RestClientConfig clientConfig, HttpClient httpClient) {
        Checks.checkNotNull(clientConfig, "ClientConfig must not be null!");
        Checks.checkNotNull(httpClient, "HttpClient must not be null!");
        this.clientConfig = clientConfig;
        this.requestExecutor = new RestRequestExecutorImpl(clientConfig);
        this.target = httpClient;
    }

    @Override
    public ExecutableRestRequest get(String uri) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public RestRequestFacade post(String uri) {
        return new RestCompositeRequest((CompositeRequest) target.post(uri), clientConfig, requestExecutor);
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
    public RestClientConfig clientConfig() {
        return clientConfig;
    }

}
