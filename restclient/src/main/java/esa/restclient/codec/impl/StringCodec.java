package esa.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import esa.restclient.codec.ByteCodec;
import io.esastack.commons.net.http.MediaType;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringCodec implements ByteCodec {

    @SuppressWarnings("unchecked")
    @Override
    public String doDecode(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) {
        if (data == null || type == null) {
            return null;
        }

        Charset charset = null;
        if (mediaType != null) {
            charset = mediaType.charset();
        }
        if (charset == null) {
            return new String(data, StandardCharsets.UTF_8);
        } else {
            return new String(data, charset);
        }
    }

    @Override
    public byte[] doEncode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }

        Charset charset = null;
        if (mediaType != null) {
            charset = mediaType.charset();
        }
        if (charset == null) {
            return ((String) entity).getBytes(StandardCharsets.UTF_8);
        } else {
            return ((String) entity).getBytes(mediaType.charset());
        }
    }
}
