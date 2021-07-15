package esa.restclient;

import esa.commons.http.HttpHeaders;

public interface RequestContentTypeFactory {
    ContentType create(HttpHeaders requestHeaders, Object entity);
}
