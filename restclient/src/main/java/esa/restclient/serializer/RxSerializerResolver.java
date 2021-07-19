package esa.restclient.serializer;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

public interface RxSerializerResolver extends Ordered {
    RxSerializer resolve(RestRequest request, ContentType[] acceptTypes,
                         MediaType mediaType, HttpHeaders responseHeaders, Type type);
}
