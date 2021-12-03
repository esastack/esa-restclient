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

import esa.commons.DateUtils;
import io.esastack.commons.net.http.MediaType;

public interface JsonCodec extends ByteCodec {
    String DEFAULT_DATE_FORMAT = DateUtils.yyyyMMddHHmmss;

    @Override
    default RequestContent<byte[]> doEncode(EncodeContext<byte[]> ctx) throws Exception {
        MediaType contentType = ctx.contentType();
        if (contentType != null && MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            return encodeToJson(ctx);
        }

        return ctx.next();
    }

    RequestContent<byte[]> encodeToJson(EncodeContext<byte[]> ctx) throws Exception;

    @Override
    default Object doDecode(DecodeContext<byte[]> ctx) throws Exception {
        MediaType contentType = ctx.contentType();
        if (contentType != null && MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            byte[] content = ctx.content().value();
            if (content == null) {
                return null;
            }
            return decodeFromJson(ctx);
        }
        return ctx.next();
    }

    Object decodeFromJson(DecodeContext<byte[]> ctx) throws Exception;
}
