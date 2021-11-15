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
import io.esastack.restclient.utils.Constants;

/**
 * <code>Decoder</code> is designed for the conversion from {@code ctx.content()} to object.And
 * in many scenarios, what you need is {@link ByteDecoder} which makes it unnecessary for you to understand
 * the {@link ResponseContent}.
 */
public interface Decoder extends Ordered {

    /**
     * Decode the {@code ctx.content()} to object.If this decoder can decode the
     * {@code ctx.content()},it will directly decode and return object.Otherwise,
     * it will call {@code ctx.next()} to hand over the decoding work to the next
     * decoder.
     *
     * @param ctx which is to save variables required during decoding
     * @return decoded object
     * @throws Exception error
     */
    Object decode(DecodeContext<?> ctx) throws Exception;

    default int getOrder() {
        return Constants.Order.NORMAL;
    }
}
