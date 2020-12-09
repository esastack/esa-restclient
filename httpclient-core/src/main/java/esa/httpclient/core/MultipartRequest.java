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

import java.util.List;

public interface MultipartRequest extends HttpRequest {

    /**
     * Obtains attributes of current multipart body, which is a unmodifiable copy of current attributes.
     *
     * @return attributes
     */
    MultiValueMap<String, String> attributes();

    /**
     * Obtains multipart files of current multipart body
     *
     * @return file items
     */
    List<MultipartFileItem> files();

    /**
     * {@link RequestType} of current request
     *
     * @return request
     */
    @Override
    default RequestType type() {
        return RequestType.MULTIPART;
    }
}
