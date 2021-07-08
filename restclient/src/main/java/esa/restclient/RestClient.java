package esa.restclient;

import esa.commons.http.HttpMethod;
import esa.httpclient.core.HttpClientBuilder;

public interface RestClient {
    /**
     * An easy way to build {@link HttpMethod#GET} request.
     *
     * @param uri request uri
     * @return builder
     */
    ExecutableRestRequest get(String uri);

    /**
     * An easy way to build {@link HttpMethod#POST} request.
     *
     * @param uri request uri
     * @return builder
     */
    FacadeRestRequest post(String uri);

    /**
     * An easy way to build {@link HttpMethod#DELETE} request.
     *
     * @param uri request uri
     * @return builder
     */
    FacadeRestRequest delete(String uri);

    /**
     * An easy way to build {@link HttpMethod#PUT} request.
     *
     * @param uri request uri
     * @return builder
     */
    FacadeRestRequest put(String uri);

    /**
     * An easy way to build {@link HttpMethod#HEAD} request.
     *
     * @param uri request uri
     * @return builder
     */
    ExecutableRestRequest head(String uri);

    /**
     * An easy way to build {@link HttpMethod#OPTIONS} request.
     *
     * @param uri request uri
     * @return builder
     */
    ExecutableRestRequest options(String uri);

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
