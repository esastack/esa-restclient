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
package esa.restclient.serializer;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.AsciiString;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoBufSerializer implements Serializer {

    private static final Map<Class<?>, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    private final ExtensionRegistry extensionRegistry;

    /**
     * The HTTP header containing the protobuf schema.
     */
    public static final AsciiString X_PROTOBUF_SCHEMA_HEADER = AsciiString.cached("X-Protobuf-Schema");

    /**
     * The HTTP header containing the protobuf message.
     */
    public static final AsciiString X_PROTOBUF_MESSAGE_HEADER = AsciiString.cached("X-Protobuf-Message");


    public ProtoBufSerializer() {
        this(ExtensionRegistry.newInstance());
    }

    public ProtoBufSerializer(ExtensionRegistry extensionRegistry) {
        this.extensionRegistry = extensionRegistry;
    }

    @Override
    public byte[] serialize(MediaType mediaType, HttpHeaders headers, Object target) {
        if (target == null) {
            return null;
        }
        if (target instanceof Message) {
            Message message = (Message) target;
            headers.set(HttpHeaderNames.CONTENT_TYPE, MediaType.PROTOBUF.value());
            headers.set(X_PROTOBUF_SCHEMA_HEADER, message.getDescriptorForType().getFile().getName());
            headers.set(X_PROTOBUF_MESSAGE_HEADER, message.getDescriptorForType().getFullName());
            return message.toByteArray();
        }
        throw new UnsupportedOperationException("Could not serialize class: " +
                target.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deSerialize(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) throws Exception {
        Message.Builder builder = getMessageBuilder((Class<? extends Message>) type);
        builder.mergeFrom(data, extensionRegistry);
        return (T) builder.build();
    }

    private Message.Builder getMessageBuilder(Class<? extends Message> clazz) throws Exception {
        Method method = METHOD_CACHE.get(clazz);
        if (method == null) {
            method = clazz.getMethod("newBuilder");
            METHOD_CACHE.put(clazz, method);
        }
        return (Message.Builder) method.invoke(clazz);
    }
}
