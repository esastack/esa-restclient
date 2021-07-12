package esa.restclient;

import esa.commons.http.HttpHeaders;

import java.lang.reflect.Type;
import java.util.Optional;

public interface ResponseContentTypeResolver {
    Optional<ContentType> resolve(RestRequest request, HttpHeaders responseHeaders, Type type);
}
