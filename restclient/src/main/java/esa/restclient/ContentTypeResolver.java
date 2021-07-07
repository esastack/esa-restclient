package esa.restclient;

import esa.commons.http.HttpHeaders;

public interface ContentTypeResolver {
    ContentType resolve(HttpHeaders requestHeaders);
}
