package esa.restclient;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;

import java.lang.reflect.Type;
import java.util.Optional;

public interface ResponseContentTypeResolver extends Ordered {
    ContentType resolve(RestRequest request, MediaType mediaType, HttpHeaders responseHeaders, Type type);
}
