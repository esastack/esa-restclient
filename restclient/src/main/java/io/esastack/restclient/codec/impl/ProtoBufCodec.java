package io.esastack.restclient.codec.impl;

import com.google.protobuf.Descriptors;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.codec.ByteCodec;
import io.esastack.restclient.codec.CodecResult;
import io.netty.util.AsciiString;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
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
    public CodecResult<byte[]> doEncode(MediaType mediaType, HttpHeaders headers,
                                        Object entity, Class<?> type, Type genericType) {

        if (PROTO_BUF.isCompatibleWith(mediaType) && Message.class.isAssignableFrom(type)) {
            Message message = (Message) entity;
            Descriptors.Descriptor descriptor = message.getDescriptorForType();
            if (descriptor != null) {
                headers.set(X_PROTOBUF_MESSAGE_HEADER, message.getDescriptorForType().getFullName());
                Descriptors.FileDescriptor fileDescriptor = message.getDescriptorForType().getFile();
                if (fileDescriptor != null) {
                    headers.set(X_PROTOBUF_SCHEMA_HEADER, message.getDescriptorForType().getFile().getName());
                }
            }
            return CodecResult.success(message.toByteArray());
        }
        return CodecResult.fail();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> CodecResult<T> doDecode(MediaType mediaType, HttpHeaders headers,
                                       byte[] content, Class<T> type, Type genericType) throws Exception {

        if (PROTO_BUF.isCompatibleWith(mediaType) && Message.class.isAssignableFrom(type)) {
            Message.Builder builder = getMessageBuilder(type);
            builder.mergeFrom(content, extensionRegistry);
            return CodecResult.success((T) builder.build());
        }

        return CodecResult.fail();
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
