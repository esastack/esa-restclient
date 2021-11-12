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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import esa.commons.Checks;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.JsonCodec;
import io.esastack.restclient.codec.RequestContent;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;

public class JacksonCodec implements JsonCodec {

    private static ObjectMapper DEFAULT_OBJECT_MAPPER;

    private final ObjectMapper objectMapper;

    public JacksonCodec() {
        this(getDefaultMapper());
    }

    public JacksonCodec(ObjectMapper objectMapper) {
        Checks.checkNotNull(objectMapper, "objectMapper");
        this.objectMapper = objectMapper;
    }

    @Override
    public RequestContent<byte[]> encodeToJson(EncodeContext<byte[]> encodeContext) throws JsonProcessingException {
        return RequestContent.of(objectMapper.writeValueAsBytes(encodeContext.entity()));
    }

    @Override
    public Object decodeFromJson(DecodeContext<byte[]> decodeContext) throws IOException {
        return objectMapper.readValue(decodeContext.content().value(), getJavaType(decodeContext.targetGenericType()));
    }

    public static synchronized ObjectMapper getDefaultMapper() {
        if (DEFAULT_OBJECT_MAPPER == null) {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT));
            objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
            objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            DEFAULT_OBJECT_MAPPER = objectMapper;
        }
        return DEFAULT_OBJECT_MAPPER;
    }

    private JavaType getJavaType(Type type) {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        return typeFactory.constructType(type);
    }

}
