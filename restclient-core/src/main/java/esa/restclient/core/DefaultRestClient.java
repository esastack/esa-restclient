package esa.restclient.core;

import esa.commons.Checks;
import esa.commons.http.HttpMethod;
import esa.httpclient.core.HttpClient;
import esa.restclient.core.request.DefaultFacadeRequest;
import esa.restclient.core.request.ExecutableRequest;
import esa.restclient.core.request.FacadeRequest;


public class DefaultRestClient implements RestClient {

    private RestClientBuilder builder;
    private HttpClient httpClient;

    DefaultRestClient(RestClientBuilder builder, HttpClient httpClient) {
        Checks.checkNotNull(builder, "Builder must not be null!");
        Checks.checkNotNull(httpClient, "HttpClient must not be null!");
        this.builder = builder;
        this.httpClient = httpClient;
    }

    @Override
    public ExecutableRequest get(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.GET, builder, this);
    }

    @Override
    public FacadeRequest post(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.POST, builder, this);
    }

    @Override
    public FacadeRequest delete(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.DELETE, builder, this);
    }

    @Override
    public FacadeRequest put(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.PUT, builder, this);
    }

    @Override
    public ExecutableRequest head(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.HEAD, builder, this);
    }

    @Override
    public ExecutableRequest options(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.OPTIONS, builder, this);
    }
}
