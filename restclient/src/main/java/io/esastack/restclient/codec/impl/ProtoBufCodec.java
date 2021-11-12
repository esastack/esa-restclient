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

import com.google.protobuf.Descriptors;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.codec.ByteCodec;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.RequestContent;
import io.netty.util.AsciiString;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoBufCodec implements ByteCodec {

    private static final Map<Class<?>, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    private final ExtensionRegistry extensionRegistry;

    public static final MediaType PROTO_BUF =
            MediaTypeUtil.of("application", "x-protobuf", StandardCharsets.UTF_8);

    /**
     * The HTTP header containing the protobuf schema.
     */
    public static final AsciiString X_PROTOBUF_SCHEMA_HEADER = AsciiString.cached("X-Protobuf-Schema");

    /**
     * The HTTP header containing the protobuf message.
     */
    public static final AsciiString X_PROTOBUF_MESSAGE_HEADER = AsciiString.cached("X-Protobuf-Message");

    public ProtoBufCodec() {
        this(ExtensionRegistry.newInstance());
    }

    public ProtoBufCodec(ExtensionRegistry extensionRegistry) {
        this.extensionRegistry = extensionRegistry;
    }

    @Override
    public RequestContent<byte[]> doEncode(EncodeContext<byte[]> encodeContext) throws Exception {
        if (PROTO_BUF.isCompatibleWith(encodeContext.contentType()) &&
                Message.class.isAssignableFrom(encodeContext.entityType())) {
            Message message = (Message) encodeContext.entity();
            Descriptors.Descriptor descriptor = message.getDescriptorForType();
            if (descriptor != null) {
                HttpHeaders headers = encodeContext.headers();
                headers.set(X_PROTOBUF_MESSAGE_HEADER, message.getDescriptorForType().getFullName());
                Descriptors.FileDescriptor fileDescriptor = message.getDescriptorForType().getFile();
                if (fileDescriptor != null) {
                    headers.set(X_PROTOBUF_SCHEMA_HEADER, message.getDescriptorForType().getFile().getName());
                }
            }
            return RequestContent.of(message.toByteArray());
        }
        return encodeContext.next();
    }

    @Override
    public Object doDecode(DecodeContext<byte[]> decodeContext) throws Exception {
        Class<?> type = decodeContext.targetType();
        if (PROTO_BUF.isCompatibleWith(decodeContext.contentType())
                && Message.class.isAssignableFrom(type)) {
            Message.Builder builder = getMessageBuilder(type);
            builder.mergeFrom(decodeContext.content().value(), extensionRegistry);
            return builder.build();
        }

        return decodeContext.next();
    }

    private Message.Builder getMessageBuilder(Class<?> clazz) throws Exception {
        Method method = METHOD_CACHE.get(clazz);
        if (method == null) {
            method = clazz.getMethod("newBuilder");
            METHOD_CACHE.put(clazz, method);
        }
        return (Message.Builder) method.invoke(clazz);
    }

}
