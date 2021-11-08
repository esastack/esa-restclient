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
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.codec.JsonCodec;

import java.lang.reflect.Type;
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
    public byte[] encodeToJson(MediaType mediaType, HttpHeaders headers,
                           Object entity, Class<?> type, Type genericType) {
        Charset charset = null;
        if (mediaType != null) {
            charset = mediaType.charset();
        }
        if (charset == null) {
            return gson.toJson(entity).getBytes(StandardCharsets.UTF_8);
        } else {
            return gson.toJson(entity).getBytes(charset);
        }
    }

    @Override
    public <T> T decodeFromJson(MediaType mediaType, HttpHeaders headers, byte[] body,
                           Class<T> type, Type genericType) {
        Charset charset = null;
        if (mediaType != null) {
            charset = mediaType.charset();
        }

        if (charset == null) {
            return gson.fromJson(new String(body, StandardCharsets.UTF_8), type);
        } else {
            return gson.fromJson(new String(body, charset), type);
        }
    }
}
