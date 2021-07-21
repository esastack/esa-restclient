package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;

import java.lang.reflect.Type;

public class ByteToByteCodec implements ByteCodec {
    @Override
    public byte[] decode(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) throws Exception {
        return data;
    }

    @Override
    public byte[] encode(MediaType mediaType, HttpHeaders headers, Object entity) throws Exception {
        if (entity == null) {
            return null;
        }

        if (entity instanceof byte[]) {
            return (byte[]) entity;
        }

        throw new UnsupportedOperationException("ByteToByteCodec " +
                "only support encode byte[] to byte[]!entityClass:" + entity.getClass());
    }
}
