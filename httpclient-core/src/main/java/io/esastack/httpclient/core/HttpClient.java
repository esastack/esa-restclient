/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.httpclient.core;

import esa.commons.http.HttpMethod;
import io.esastack.httpclient.core.metrics.MetricPoint;

import java.io.Closeable;

/**
 * The facade class for preparing a {@link SegmentRequest} or executing a common {@link HttpRequest}.
 */
public interface HttpClient extends Closeable, Identifiable, MetricPoint {

    /**
     * An easy way to build {@link HttpMethod#GET} request.
     *
     * @param uri request uri
     * @return builder
     */
    HttpRequestFacade get(String uri);

    /**
     * An easy way to build {@link HttpMethod#POST} request.
     *
     * @param uri request uri
     * @return builder
     */
    HttpRequestFacade post(String uri);

    /**
     * An easy way to build {@link HttpMethod#DELETE} request.
     *
     * @param uri request uri
     * @return builder
     */
    HttpRequestFacade delete(String uri);

    /**
     * An easy way to build {@link HttpMethod#PUT} request.
     *
     * @param uri request uri
     * @return builder
     */
    HttpRequestFacade put(String uri);

    /**
     * An easy way to build {@link HttpMethod#GET} request.
     *
     * @param uri request uri
     * @return builder
     */
    HttpRequestFacade head(String uri);

    /**
     * An easy way to build {@link HttpMethod#CONNECT} request.
     *
     * @param uri request uri
     * @return builder
     */
    HttpRequestFacade connect(String uri);

    /**
     * An easy way to build {@link HttpMethod#OPTIONS} request.
     *
     * @param uri request uri
     * @return builder
     */
    HttpRequestFacade options(String uri);

    /**
     * An easy way to build {@link HttpMethod#TRACE} request.
     *
     * @param uri request uri
     * @return builder
     */
    HttpRequestFacade trace(String uri);

    /**
     * An easy way to build {@link HttpMethod#PATCH} request.
     *
     * @param uri       request uri
     * @return builder
     */
    HttpRequestFacade patch(String uri);

    ////////*********************** BUILDER TEMPLATE ********************************////////

    /**
     * An easy way to obtain a {@link HttpClient} conveniently
     *
     * @return {@link HttpClient}
     */
    static HttpClient ofDefault() {
        return new HttpClientBuilder().build();
    }

    /**
     * An easy way to obtain {@link HttpClientBuilder} conveniently
     *
     * @return {@link HttpClientBuilder}
     */
    static HttpClientBuilder create() {
        return new HttpClientBuilder();
    }
}
