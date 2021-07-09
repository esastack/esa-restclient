package esa.restclient;

import esa.commons.http.HttpHeaders;

import java.lang.reflect.Type;
import java.util.Optional;

public interface AcceptTypeResolver {
    Optional<AcceptType> resolve(RestRequest request, RequestContext context, HttpHeaders responseHeaders, Type type);
}
