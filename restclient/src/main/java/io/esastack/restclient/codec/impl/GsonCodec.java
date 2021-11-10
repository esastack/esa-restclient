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
package io.esastack.restclient.codec.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.JsonCodec;
import io.esastack.restclient.codec.RequestContent;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GsonCodec implements JsonCodec {

    private final Gson gson;

    public GsonCodec() {
        this(null);
    }

    public GsonCodec(GsonBuilder gsonBuilder) {
        if (gsonBuilder != null) {
            gson = gsonBuilder.create();
        } else {
            gson = new GsonBuilder().setDateFormat(DEFAULT_DATE_FORMAT).create();
        }
    }

    @Override
    public RequestContent<byte[]> encodeToJson(EncodeContext<byte[]> encodeContext) {
        MediaType contentType = encodeContext.contentType();
        Charset charset = null;
        if (contentType != null) {
            charset = contentType.charset();
        }
        if (charset == null) {
            return RequestContent.of(gson.toJson(encodeContext.entity()).getBytes(StandardCharsets.UTF_8));
        } else {
            return RequestContent.of(gson.toJson(encodeContext.entity()).getBytes(charset));
        }
    }

    @Override
    public Object decodeFromJson(DecodeContext<byte[]> decodeContext) {
        MediaType contentType = decodeContext.contentType();
        Charset charset = null;
        if (contentType != null) {
            charset = contentType.charset();
        }

        if (charset == null) {
            return gson.fromJson(
                    new String(decodeContext.content().value(), StandardCharsets.UTF_8), decodeContext.genericType());
        } else {
            return gson.fromJson(
                    new String(decodeContext.content().value(), charset), decodeContext.genericType());
        }
    }
}
