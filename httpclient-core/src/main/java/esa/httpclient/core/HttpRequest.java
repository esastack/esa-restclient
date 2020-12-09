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
package esa.httpclient.core;

import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import esa.httpclient.core.HttpRequestBuilder.BodyForbiddenBuilder;
import esa.httpclient.core.HttpRequestBuilder.BodyPermittedBuilder;

import java.util.List;
import java.util.Set;

public interface HttpRequest {

    /**
     * Obtains method name
     *
     * @return method
     */
    HttpMethod method();

    /**
     * Obtains scheme
     *
     * @return scheme
     */
    String scheme();

    /**
     * Obtains path of current request
     *
     * @return  path
     */
    String path();

    /**
     * Obtains original request's uri as string format.
     *
     * @return uri
     */
    HttpUri uri();

    /**
     * Adds param.
     *
     * @param name  name
     * @param value value
     *
     * @return this
     */
    HttpRequest addParam(String name, String value);

    /**
     * Obtains params by name
     *
     * @param name name
     * @return value
     */
    String getParam(String name);

    /**
     * Obtains param values as a copy of specified {@code name}.
     *
     * @param name name
     * @return value
     */
    List<String> getParams(String name);

    /**
     * Obtains a copy of current parameter names.
     *
     * @return names
     */
    Set<String> paramNames();

    /**
     * Obtains {@link HttpHeaders}
     *
     * @return headers
     */
    HttpHeaders headers();

    /**
     * Adds header
     *
     * @param name  name
     * @param value value
     *
     * @return this
     */
    HttpRequest addHeader(CharSequence name, CharSequence value);

    /**
     * Gets header
     *
     * @param name name
     * @return value
     */
    CharSequence getHeader(CharSequence name);

    /**
     * Sets header
     *
     * @param name  name
     * @param value value
     *
     * @return this
     */
    HttpRequest setHeader(CharSequence name, CharSequence value);

    /**
     * Removes header
     *
     * @param name name
     *
     * @return this
     */
    HttpRequest removeHeader(CharSequence name);

    /**
     * Obtains the {@link RequestType} of current request
     *
     * @return type
     */
    default RequestType type() {
        return RequestType.PLAIN;
    }

    /**
     * Obtains the original {@link RequestOptions}. You must be aware that the modification of
     * the result config has impact of current {@link HttpRequest} instance.
     *
     * @return current options
     */
    RequestOptions config();

    /**
     * An easy way to build {@link HttpMethod#GET} request.
     *
     * @param uri       request uri
     * @return builder
     */
    static BodyForbiddenBuilder get(String uri) {
        return new BodyForbiddenBuilder(HttpMethod.GET, uri);
    }

    /**
     * An easy way to build {@link HttpMethod#GET} request.
     *
     * @param uri       request uri
     * @return builder
     */
    static BodyForbiddenBuilder head(String uri) {
        return new BodyForbiddenBuilder(HttpMethod.HEAD, uri);
    }

    /**
     * An easy way to build {@link HttpMethod#OPTIONS} request.
     *
     * @param uri       request uri
     * @return builder
     */
    static BodyForbiddenBuilder options(String uri) {
        return new BodyForbiddenBuilder(HttpMethod.OPTIONS, uri);
    }

    /**
     * An easy way to build {@link HttpMethod#TRACE} request.
     *
     * @param uri       request uri
     * @return builder
     */
    static BodyForbiddenBuilder trace(String uri) {
        return new BodyForbiddenBuilder(HttpMethod.TRACE, uri);
    }

    /**
     * An easy way to build {@link HttpMethod#CONNECT} request.
     *
     * @param uri       request uri
     * @return builder
     */
    static BodyForbiddenBuilder connect(String uri) {
        return new BodyForbiddenBuilder(HttpMethod.CONNECT, uri);
    }

    /**
     * An easy way to build {@link HttpMethod#POST} request.
     *
     * @param uri       request uri
     * @return builder
     */
    static BodyPermittedBuilder post(String uri) {
        return new BodyPermittedBuilder(HttpMethod.POST, uri);
    }

    /**
     * An easy way to build {@link HttpMethod#DELETE} request.
     *
     * @param uri       request uri
     * @return builder
     */
    static BodyPermittedBuilder delete(String uri) {
        return new BodyPermittedBuilder(HttpMethod.DELETE, uri);
    }

    /**
     * An easy way to build {@link HttpMethod#PUT} request.
     *
     * @param uri       request uri
     * @return builder
     */
    static BodyPermittedBuilder put(String uri) {
        return new BodyPermittedBuilder(HttpMethod.PUT, uri);
    }

    /**
     * An easy way to build {@link HttpMethod#PATCH} request.
     *
     * @param uri       request uri
     * @return builder
     */
    static BodyPermittedBuilder patch(String uri) {
        return new BodyPermittedBuilder(HttpMethod.PATCH, uri);
    }

    /**
     * An easy way to build {@link MultipartRequest}.
     *
     * @param uri       request uri
     * @return builder
     */
    static HttpRequestBuilder.Multipart multipart(String uri) {
        return new HttpRequestBuilder.Multipart(HttpMethod.POST, uri);
    }
}
