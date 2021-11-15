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

import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.codec.ByteCodec;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.RequestContent;
import io.esastack.restclient.utils.Constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringCodec implements ByteCodec {

    @Override
    public RequestContent<byte[]> doEncode(EncodeContext<byte[]> ctx) throws Exception {
        if (String.class.isAssignableFrom(ctx.entityType())) {
            MediaType contentType = ctx.contentType();
            Charset charset = null;
            if (contentType != null) {
                charset = contentType.charset();
            }
            if (charset == null) {
                return RequestContent.of(((String) ctx.entity()).getBytes(StandardCharsets.UTF_8));
            } else {
                return RequestContent.of(((String) ctx.entity()).getBytes(charset));
            }
        }
        return ctx.next();
    }

    @Override
    public Object doDecode(DecodeContext<byte[]> ctx) throws Exception {
        if (String.class.isAssignableFrom(ctx.targetType())) {
            MediaType contentType = ctx.contentType();
            Charset charset = null;
            if (contentType != null) {
                charset = contentType.charset();
            }
            if (charset == null) {
                return new String(ctx.content().value(), StandardCharsets.UTF_8);
            } else {
                return new String(ctx.content().value(), charset);
            }
        }
        return ctx.next();
    }

    @Override
    public int getOrder() {
        return Constants.Order.STRING_CODEC;
    }

}
