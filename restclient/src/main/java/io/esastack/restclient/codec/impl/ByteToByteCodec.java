package io.esastack.restclient.codec.impl;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.util.Ordered;
import io.esastack.restclient.codec.ByteCodec;
import io.esastack.restclient.codec.CodecResult;

import java.lang.reflect.Type;

public class ByteToByteCodec implements ByteCodec {

    @Override
    public CodecResult<byte[]> doEncode(MediaType mediaType, HttpHeaders headers,
                                        Object entity, Class<?> type, Type genericType) {
        if (type.isArray() && type.getComponentType().equals(byte.class)) {
            return CodecResult.success((byte[]) entity);
        }

        return CodecResult.fail();
    }

    @Override
    public <T> CodecResult<T> doDecode(MediaType mediaType, HttpHeaders headers,
                                       byte[] content, Class<T> type, Type genericType) {
        if (type.isArray() && type.getComponentType().equals(byte.class)) {
            return CodecResult.success((T) content);
        }

        return CodecResult.fail();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWER_PRECEDENCE;
    }
}
