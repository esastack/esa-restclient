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
package io.esastack.restclient.codec.impl;

import io.esastack.httpclient.core.util.Ordered;
import io.esastack.restclient.codec.ByteCodec;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.RequestContent;

public class ByteToByteCodec implements ByteCodec {

    @Override
    public RequestContent<byte[]> doEncode(EncodeContext<byte[]> encodeContext) throws Exception {
        Class<?> type = encodeContext.type();
        if (type.isArray() && type.getComponentType().equals(byte.class)) {
            return RequestContent.of((byte[]) encodeContext.entity());
        }

        return encodeContext.next();
    }

    @Override
    public Object doDecode(DecodeContext<byte[]> decodeContext) throws Exception {
        Class<?> type = decodeContext.type();
        if (type.isArray() && type.getComponentType().equals(byte.class)) {
            return decodeContext.content().value();
        }

        return decodeContext.next();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWER_PRECEDENCE;
    }

}
