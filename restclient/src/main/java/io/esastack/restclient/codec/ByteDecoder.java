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

public interface ByteDecoder extends Decoder {

    @SuppressWarnings("unchecked")
    @Override
    default Object decode(DecodeContext<?> ctx) throws Exception {
        if (ctx.content().value() instanceof byte[]) {
            return doDecode((DecodeContext<byte[]>) ctx);
        }
        return ctx.next();
    }

    Object doDecode(DecodeContext<byte[]> ctx) throws Exception;

}
