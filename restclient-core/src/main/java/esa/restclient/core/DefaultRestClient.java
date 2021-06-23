package esa.restclient.core;

import esa.commons.Checks;
import esa.commons.http.HttpMethod;
import esa.httpclient.core.HttpClient;
import esa.restclient.core.request.DefaultFacadeRequest;
import esa.restclient.core.request.ExecutableRequest;
import esa.restclient.core.request.FacadeRequest;


public class DefaultRestClient implements RestClient {

    private final RestClientConfig clientConfig;
    private final HttpClient httpClient;

    DefaultRestClient(RestClientConfig clientConfig, HttpClient httpClient) {
        Checks.checkNotNull(clientConfig, "ClientConfig must not be null!");
        Checks.checkNotNull(httpClient, "HttpClient must not be null!");
        this.clientConfig = clientConfig;
        this.httpClient = httpClient;
    }

    @Override
    public ExecutableRequest get(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.GET, clientConfig.version(), this);
    }

    @Override
    public FacadeRequest post(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.POST, clientConfig.version(), this);
    }

    @Override
    public FacadeRequest delete(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.DELETE, clientConfig.version(), this);
    }

    @Override
    public FacadeRequest put(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.PUT, clientConfig.version(), this);
    }

    @Override
    public ExecutableRequest head(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.HEAD, clientConfig.version(), this);
    }

    @Override
    public ExecutableRequest options(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.OPTIONS, clientConfig.version(), this);
    }

    @Override
    public RestClientConfig clientConfig() {
        return clientConfig;
    }
}
