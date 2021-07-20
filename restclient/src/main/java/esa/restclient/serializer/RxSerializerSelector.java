package esa.restclient.serializer;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

public interface RxSerializerSelector extends Ordered {


    int HIGHER_PRECEDENCE = -2048;

    int MIDDLE_PRECEDENCE = 0;

    int LOWER_PRECEDENCE = 2048;


    RxSerializer select(RestRequest request, ContentType[] acceptTypes,
                        MediaType responseMediaType, HttpHeaders responseHeaders, Type type);

    @Override
    default int getOrder() {
        return MIDDLE_PRECEDENCE;
    }
}
