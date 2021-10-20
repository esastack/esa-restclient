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

import esa.commons.collection.MultiValueMap;
import esa.commons.netty.core.Buffer;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface HttpRequest extends Request, Multipart, Reusable<HttpRequest> {

    @Override
    HttpRequest addParam(String name, String value);

    @Override
    HttpRequest addParams(Map<String, String> params);

    @Override
    HttpRequest addHeader(CharSequence name, CharSequence value);

    @Override
    HttpRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    HttpRequest setHeader(CharSequence name, CharSequence value);

    @Override
    HttpRequest removeHeader(CharSequence name);

    /**
     * Whether use multipart encode for {@link MultipartRequest}.
     *
     * @return {@code true} if current request using multipart encode, otherwise {@code false}.
     * the return value will be always {@code false} when {@link #isMultipart()}
     * is false.
     */
    default boolean multipartEncode() {
        return false;
    }

    /**
     * Whether segment write or not.
     *
     * @return {@code true} if current request is segment write, otherwise {@code false}.
     */
    default boolean isSegmented() {
        return false;
    }

    /**
     * Whether multipart or not.
     *
     * @return {@code true} if current request is {@link MultipartRequest}, otherwise {@code false}.
     */
    default boolean isMultipart() {
        return false;
    }

    /**
     * Whether file content is regarded as body or not.
     *
     * @return {@code true} if current request's body is the content of {@link #file()}, otherwise {@code false}.
     */
    default boolean isFile() {
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
     * @return attrs, must be null if {@link #isMultipart()} is {@code false}.
     */
    default MultiValueMap<String, String> attrs() {
        return null;
    }

    /**
     * Obtains the files which are used to multipart encoded.
     *
     * @return files, must be null if {@link #isMultipart()} is {@code false}.
     */
    default List<MultipartFileItem> files() {
        return null;
    }

}
