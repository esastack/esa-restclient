package esa.restclient;

import esa.commons.http.HttpHeaders;

import java.util.Optional;

public interface ContentTypeFactory {
    Optional<ContentType> create(HttpHeaders requestHeaders, RequestContext context, Object entity);
}
