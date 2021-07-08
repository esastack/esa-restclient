package esa.restclient;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.Request;

import java.lang.reflect.Type;
import java.util.Optional;

public interface AcceptTypeResolver {
    Optional<AcceptType> resolve(Request request, RequestContext context, HttpHeaders responseHeaders, Type type);
}
