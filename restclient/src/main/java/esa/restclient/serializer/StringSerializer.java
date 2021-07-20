package esa.restclient.serializer;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringSerializer implements Serializer {

    public static final StringSerializer INSTANCE
            = new StringSerializer();

    @SuppressWarnings("unchecked")
    @Override
    public String deSerialize(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) throws Exception {
        if (data == null || type == null) {
            return null;
        }

        if (type instanceof Class &&
                ((Class<?>) type).isAssignableFrom(String.class)
        ) {
            Charset charset = mediaType.charset();
            if (charset == null) {
                return new String(data, StandardCharsets.UTF_8);
            } else {
                return new String(data, charset);
            }

        }

        throw new UnsupportedOperationException("StringSerializer " +
                "only can deSerialize String.class and its parentClass");
    }

    @Override
    public byte[] serialize(MediaType mediaType, HttpHeaders headers, Object target) throws Exception {
        if (target == null) {
            return null;
        }

        if (target instanceof String) {
            Charset charset = mediaType.charset();
            if (charset == null) {
                return ((String) target).getBytes(StandardCharsets.UTF_8);
            } else {
                return ((String) target).getBytes(mediaType.charset());
            }
        }

        throw new UnsupportedOperationException("StringSerializer " +
                "only can serialize String.class and its subClass");
    }
}
