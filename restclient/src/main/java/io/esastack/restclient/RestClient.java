/*
 * Copyright 2021 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
