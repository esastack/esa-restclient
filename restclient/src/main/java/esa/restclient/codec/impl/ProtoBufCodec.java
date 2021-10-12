package esa.restclient.codec.impl;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;
import esa.restclient.codec.ByteCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
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
        if (entity instanceof Message) {
            Message message = (Message) entity;
            headers.set(HttpHeaderNames.CONTENT_TYPE, MediaType.PROTOBUF.value());
            headers.set(X_PROTOBUF_SCHEMA_HEADER, message.getDescriptorForType().getFile().getName());
            headers.set(X_PROTOBUF_MESSAGE_HEADER, message.getDescriptorForType().getFullName());
            return message.toByteArray();
        }
        throw new UnsupportedOperationException("Could not serialize class: " +
                entity.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T doDecode(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) throws Exception {
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
