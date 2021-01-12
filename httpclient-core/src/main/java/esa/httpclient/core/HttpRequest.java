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

import esa.commons.collection.MultiValueMap;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import esa.commons.netty.core.Buffer;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface HttpRequest extends Reusable<HttpRequest> {

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
     * Whether allow uri encode or not
     *
     * @return true or false
     */
    boolean uriEncodeEnabled();

    /**
     * The readTimeout of current request
     *
     * @return readTimeout
     */
    int readTimeout();

    /**
     * Whether chunk write or not.
     *
     * @return true if current request is chunk write, otherwise false.
     */
    default boolean isSegmented() {
        return false;
    }

    /**
     * Whether multipart or not.
     *
     * @return true is current request using multipart encode, otherwise false.
     */
    default boolean isMultipart() {
        return false;
    }

    /**
     * Obtains given {@code byte[]} which is regarded as request's body.
     *
     * @return buffer, which may be null if you haven't set the {@code byte[]} before.
     */
    default Buffer buffer() {
        return null;
    }

    /**
     * Obtains given {@code file} which is regarded as request's body.
     *
     * @return file, which may be null if you haven't set the {@code file} before.
     */
    default File file() {
        return null;
    }

    /**
     * Obtains the attrs which are used to multipart encoded.
     *
     * @return attrs, must be null if {@link #isMultipart()} is false.
     */
    default MultiValueMap<String, String> attrs() {
        return null;
    }

    /**
     * Obtains the files which are used to multipart encoded.
     *
     * @return files, must be null if {@link #isMultipart()} is false.
     */
    default List<MultipartFileItem> files() {
        return null;
    }

}
