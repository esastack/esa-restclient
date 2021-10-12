package esa.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;
import esa.restclient.codec.ByteCodec;

import java.lang.reflect.Type;

public class ByteToByteCodec implements ByteCodec {
    @Override
    public byte[] doDecode(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) {
        return data;
    }

    @Override
    public byte[] doEncode(MediaType mediaType, HttpHeaders headers, Object entity) {
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
