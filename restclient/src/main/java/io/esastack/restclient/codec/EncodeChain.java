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
package io.esastack.restclient.codec;


import io.esastack.commons.net.http.MediaType;

import java.lang.reflect.Type;

public interface EncodeChain {

    /**
     * @return The contentType of request
     */
    MediaType contentType();

    /**
     * @return The entity of request
     */
    Object entity();

    /**
     * @return The type of entity
     */
    Class<?> entityType();

    /**
     * @return The generics of entity
     */
    Type entityGenerics();

    /**
     * Proceed to the next member in the chain.
     *
     * @return encoded requestContent
     * @throws Exception error
     */
    RequestContent<?> next() throws Exception;
}
