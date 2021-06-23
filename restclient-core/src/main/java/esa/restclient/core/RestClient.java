package esa.restclient.core;

import esa.commons.http.HttpMethod;
import esa.httpclient.core.HttpClientBuilder;
import esa.restclient.core.request.FacadeRequest;
import esa.restclient.core.request.ExecutableRequest;

public interface RestClient {
    /**
     * An easy way to build {@link HttpMethod#GET} request.
     *
     * @param uri request uri
     * @return builder
     */
    ExecutableRequest get(String uri);

    /**
     * An easy way to build {@link HttpMethod#POST} request.
     *
     * @param uri request uri
     * @return builder
     */
    FacadeRequest post(String uri);

    /**
     * An easy way to build {@link HttpMethod#DELETE} request.
     *
     * @param uri request uri
     * @return builder
     */
    FacadeRequest delete(String uri);

    /**
     * An easy way to build {@link HttpMethod#PUT} request.
     *
     * @param uri request uri
     * @return builder
     */
    FacadeRequest put(String uri);

    /**
     * An easy way to build {@link HttpMethod#HEAD} request.
     *
     * @param uri request uri
     * @return builder
     */
    ExecutableRequest head(String uri);

    /**
     * An easy way to build {@link HttpMethod#OPTIONS} request.
     *
     * @param uri request uri
     * @return builder
     */
    ExecutableRequest options(String uri);

    RestClientConfig clientConfig();

    /**
     * An easy way to obtain a {@link RestClient} conveniently
     *
     * @return {@link RestClient}
     */
    static RestClient ofDefault() {
        return new RestClientBuilder().build();
    }

    static RestClientBuilder create() {
        return new RestClientBuilder();
    }

    static RestClientBuilder create(HttpClientBuilder httpClientBuilder) {
        return new RestClientBuilder(httpClientBuilder);
    }
}
