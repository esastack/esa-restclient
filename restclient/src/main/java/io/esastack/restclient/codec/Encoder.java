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

import io.esastack.httpclient.core.util.Ordered;

/**
 * <code>Encoder</code> is designed for the conversion from Java type to {@link RequestContent}.And
 * in many scenarios, what you need is {@link ByteEncoder} which makes it unnecessary for you to understand
 * the {@link RequestContent}.
 */
public interface Encoder extends Ordered {

    /**
     * Encode the {@code ctx.entity()} to {@link RequestContent}.If this encoder can encode
     * the entity,it will directly encode and return {@link RequestContent}. Otherwise, it will call
     * {@code ctx.next()} to hand over the encoding work to the next encoder.
     *
     * @param ctx which is to save variables required during encoding
     * @return encoded requestContent
     * @throws Exception error
     */
    RequestContent<?> encode(EncodeContext<?> ctx) throws Exception;
}
