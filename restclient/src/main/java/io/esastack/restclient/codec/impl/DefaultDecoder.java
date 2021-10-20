package io.esastack.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.codec.ByteDecoder;

import java.lang.reflect.Type;

public class DefaultDecoder implements ByteDecoder {

    private final ByteToByteCodec byteCodec = new ByteToByteCodec();
    private final StringCodec stringCodec = new StringCodec();
    private final JacksonCodec jsonCodec = new JacksonCodec();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T doDecode(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) throws Exception {
        if (data == null || type == null) {
            return null;
        }
        if (type == byte[].class) {
            return (T) byteCodec.doDecode(mediaType, headers, data, type);
        } else if (type == String.class) {
            return (T) stringCodec.doDecode(mediaType, headers, data, type);
        } else {
            return jsonCodec.doDecode(mediaType, headers, data, type);
        }
    }
}
