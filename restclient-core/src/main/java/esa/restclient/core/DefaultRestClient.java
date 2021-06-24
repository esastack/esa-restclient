package esa.restclient.core;

import esa.commons.Checks;
import esa.commons.http.HttpMethod;
import esa.httpclient.core.HttpClient;
import esa.restclient.core.codec.CodecManager;
import esa.restclient.core.codec.DefaultCodecManager;
import esa.restclient.core.exec.DefaultRestRequestExecutor;
import esa.restclient.core.exec.RestRequestExecutor;
import esa.restclient.core.request.DefaultFacadeRequest;
import esa.restclient.core.request.ExecutableRequest;
import esa.restclient.core.request.FacadeRequest;


public class DefaultRestClient implements RestClient {

    private final RestClientConfig clientConfig;
    private final RestRequestExecutor requestExecutor;

    DefaultRestClient(RestClientConfig clientConfig, HttpClient httpClient) {
        Checks.checkNotNull(clientConfig, "ClientConfig must not be null!");
        Checks.checkNotNull(httpClient, "HttpClient must not be null!");
        this.clientConfig = clientConfig;
        this.requestExecutor = new DefaultRestRequestExecutor(httpClient, clientConfig,
                new DefaultCodecManager(clientConfig.decoders(), clientConfig.encoders()));
    }


    @Override
    public ExecutableRequest get(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.GET, clientConfig, requestExecutor);
    }

    @Override
    public FacadeRequest post(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.POST, clientConfig, requestExecutor);
    }

    @Override
    public FacadeRequest delete(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.DELETE, clientConfig, requestExecutor);
    }

    @Override
    public FacadeRequest put(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.PUT, clientConfig, requestExecutor);
    }

    @Override
    public ExecutableRequest head(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.HEAD, clientConfig, requestExecutor);
    }

    @Override
    public ExecutableRequest options(String uri) {
        return new DefaultFacadeRequest(uri, HttpMethod.OPTIONS, clientConfig, requestExecutor);
    }

    @Override
    public RestClientConfig clientConfig() {
        return clientConfig;
    }
}
