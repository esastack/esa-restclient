package io.esastack.restclient;

import io.esastack.commons.net.http.HttpMethod;

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
    RestRequestFacade post(String uri);

    /**
     * An easy way to build {@link HttpMethod#DELETE} request.
     *
     * @param uri request uri
     * @return builder
     */
    RestRequestFacade delete(String uri);

    /**
     * An easy way to build {@link HttpMethod#PUT} request.
     *
     * @param uri request uri
     * @return builder
     */
    RestRequestFacade put(String uri);

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

    RestClientOptions clientOptions();

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

}
