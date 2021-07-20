package esa.restclient.serializer;

import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

public class ByteArrayRxSerializerSelector implements RxSerializerSelector {
    public static final ByteArrayRxSerializerSelector INSTANCE = new ByteArrayRxSerializerSelector();

    private ByteArrayRxSerializerSelector(){
    }

    @Override
    public RxSerializer select(RestRequest request, ContentType[] acceptTypes,
                               MediaType responseMediaType, HttpHeaders responseHeaders, Type type) {
        if (byte[].class.equals(type)
        ) {
            return ByteArraySerializer.INSTANCE;
        } else {
            return null;
        }
    }
}
