package esa.restclient.serializer;

import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

public class RxTypeMatchResolver implements RxSerializerResolver {

    public static final RxTypeMatchResolver INSTANCE = new RxTypeMatchResolver();

    private RxTypeMatchResolver() {
    }

    @Override
    public RxSerializer resolve(RestRequest request, ContentType[] acceptTypes, MediaType mediaType, HttpHeaders responseHeaders, Type type) {
        if (String.class.equals(type)) {
            return StringSerializer.of(mediaType.charset());
        } else if (byte[].class.equals(type)) {
            return ByteArraySerializer.INSTANCE;
        } else {
            return new JacksonSerializer();
        }
    }
}
