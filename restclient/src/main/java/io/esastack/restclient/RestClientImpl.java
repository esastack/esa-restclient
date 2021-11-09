package io.esastack.restclient;

import io.esastack.httpclient.core.CompositeRequest;
import io.esastack.httpclient.core.HttpClient;

public class RestClientImpl implements RestClient {

    private final ClientInnerComposition clientInnerComposition;

    RestClientImpl(RestClientOptions clientOptions, HttpClient httpClient) {
        this.clientInnerComposition = new ClientInnerCompositionImpl(clientOptions, httpClient);
    }

    @Override
    public ExecutableRestRequest get(String uri) {
        return new RestCompositeRequest((CompositeRequest) clientInnerComposition.httpClient().get(uri),
                clientInnerComposition);
    }

    @Override
    public RestRequestFacade post(String uri) {
        return new RestCompositeRequest((CompositeRequest) clientInnerComposition.httpClient().post(uri),
                clientInnerComposition);
    }

    @Override
    public RestRequestFacade delete(String uri) {
        return new RestCompositeRequest((CompositeRequest) clientInnerComposition.httpClient().delete(uri),
                clientInnerComposition);
    }

    @Override
    public RestRequestFacade put(String uri) {
        return new RestCompositeRequest((CompositeRequest) clientInnerComposition.httpClient().put(uri),
                clientInnerComposition);
    }

    @Override
    public ExecutableRestRequest head(String uri) {
        return new RestCompositeRequest((CompositeRequest) clientInnerComposition.httpClient().head(uri),
                clientInnerComposition);
    }

    @Override
    public ExecutableRestRequest options(String uri) {
        return new RestCompositeRequest((CompositeRequest) clientInnerComposition.httpClient()
                .options(uri), clientInnerComposition);
    }

    @Override
    public RestClientOptions clientOptions() {
        return clientInnerComposition.clientOptions();
    }

}
