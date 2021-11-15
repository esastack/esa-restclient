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
package io.esastack.httpclient.core.util;

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferUtil;
import io.netty.buffer.ByteBuf;

public class BufferUtils {

    private BufferUtils() {

    }

    public static ByteBuf toByteBuf(Buffer buffer) {
        Object unwrap = BufferUtil.unwrap(buffer);
        if (unwrap instanceof ByteBuf) {
            return (ByteBuf) unwrap;
        }
        throw new UnsupportedOperationException("The type of unwrap is not ByteBuf! unwrap : " + unwrap);
    }

}
