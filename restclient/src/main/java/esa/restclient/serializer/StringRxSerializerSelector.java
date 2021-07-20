package esa.restclient.serializer;

import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

public class StringRxSerializerSelector implements RxSerializerSelector {

    public StringRxSerializerSelector() {
    }

    @Override
    public RxSerializer select(RestRequest request, ContentType[] acceptTypes,
                               MediaType mediaType, HttpHeaders responseHeaders, Type type) {
        if (String.class.equals(type)
        ) {
            return StringSerializer.INSTANCE;
        } else {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return LOWER_PRECEDENCE;
    }
}
