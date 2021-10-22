package io.esastack.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.codec.ByteCodec;

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

        return (byte[]) entity;
    }
}