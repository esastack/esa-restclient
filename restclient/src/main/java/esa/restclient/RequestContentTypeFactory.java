package esa.restclient;

import esa.commons.http.HttpHeaders;

import java.util.Optional;

public interface RequestContentTypeFactory {
    Optional<ContentType> create(HttpHeaders requestHeaders, RequestContext context, Object entity);
}
