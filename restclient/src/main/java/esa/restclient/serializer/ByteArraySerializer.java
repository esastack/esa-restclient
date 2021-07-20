package esa.restclient.serializer;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;

import java.lang.reflect.Type;

public class ByteArraySerializer implements Serializer {

    public static final ByteArraySerializer INSTANCE
            = new ByteArraySerializer();

    @Override
    public byte[] serialize(MediaType mediaType, HttpHeaders headers, Object target) {
        if (target == null) {
            return null;
        }
        
        return (byte[]) target;
    }

    @Override
    public byte[] deSerialize(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) {
        return data;
    }


}
