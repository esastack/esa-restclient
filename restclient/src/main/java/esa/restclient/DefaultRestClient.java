package esa.restclient;

import esa.commons.Checks;
import esa.httpclient.core.HttpClient;
import esa.restclient.exec.DefaultRestRequestExecutor;
import esa.restclient.exec.RestRequestExecutor;

public class DefaultRestClient implements RestClient {

    private final RestClientConfig clientConfig;
    private final RestRequestExecutor requestExecutor;

    DefaultRestClient(RestClientConfig clientConfig, HttpClient httpClient) {
        Checks.checkNotNull(clientConfig, "ClientConfig must not be null!");
        Checks.checkNotNull(httpClient, "HttpClient must not be null!");
        this.clientConfig = clientConfig;
        this.requestExecutor = new DefaultRestRequestExecutor(httpClient, clientConfig,
                null);
    }


    @Override
    public ExecutableRequest get(String uri) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public FacadeRequest post(String uri) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public FacadeRequest delete(String uri) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public FacadeRequest put(String uri) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public ExecutableRequest head(String uri) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public ExecutableRequest options(String uri) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    @Override
    public RestClientConfig clientConfig() {
        return clientConfig;
    }

}
