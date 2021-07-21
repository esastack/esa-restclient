package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringCodec implements ByteCodec {

    @SuppressWarnings("unchecked")
    @Override
    public String decode(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) throws Exception {
        if (data == null || type == null) {
            return null;
        }

        Charset charset = mediaType.charset();
        if (charset == null) {
            return new String(data, StandardCharsets.UTF_8);
        } else {
            return new String(data, charset);
        }
    }

    @Override
    public byte[] encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof String) {
            Charset charset = mediaType.charset();
            if (charset == null) {
                return ((String) entity).getBytes(StandardCharsets.UTF_8);
            } else {
                return ((String) entity).getBytes(mediaType.charset());
            }
        }

        throw new UnsupportedOperationException("FileToFileEncoder " +
                "only support encode string to byte[]!entityClass:" + entity.getClass());
    }
}
