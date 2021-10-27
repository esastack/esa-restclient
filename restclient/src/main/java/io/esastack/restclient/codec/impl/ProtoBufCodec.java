package io.esastack.restclient.codec.impl;

import com.google.protobuf.Descriptors;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.codec.ByteCodec;
import io.netty.util.AsciiString;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoBufCodec implements ByteCodec {

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


    public ProtoBufCodec() {
        this(ExtensionRegistry.newInstance());
    }

    public ProtoBufCodec(ExtensionRegistry extensionRegistry) {
        this.extensionRegistry = extensionRegistry;
    }

    @Override
    public byte[] doEncode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }

        Message message = (Message) entity;
        Descriptors.Descriptor descriptor = message.getDescriptorForType();
        if (descriptor != null) {
            headers.set(X_PROTOBUF_MESSAGE_HEADER, message.getDescriptorForType().getFullName());
            Descriptors.FileDescriptor fileDescriptor = message.getDescriptorForType().getFile();
            if (fileDescriptor != null) {
                headers.set(X_PROTOBUF_SCHEMA_HEADER, message.getDescriptorForType().getFile().getName());
            }
        }
        return message.toByteArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T doDecode(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) throws Exception {
        if (type == null || data == null) {
            return null;
        }
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
