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

import io.esastack.restclient.RestRequest;

import java.lang.reflect.Type;

/**
 * Context class used by {@link EncodeAdvice} to intercept the call of
 * {@link Encoder#encode}.
 * The member variables in this context class correspond to the
 * parameters of the intercepted method {@link Encoder#encode}
 *
 * @see Encoder
 * @see EncodeAdvice
 */
public interface EncodeAdviceContext extends EncodeChain {

    RestRequest request();

    /**
     * set entity,this method is not safe for use by multiple threads
     *
     * @param entity entity
     */
    void entity(Object entity);

    /**
     * set entity and genericType,this method is not safe for use by multiple threads
     *
     * @param entity      entity
     * @param genericType genericType
     */
    void entity(Object entity, Type genericType);
}
