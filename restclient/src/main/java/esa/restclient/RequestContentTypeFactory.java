package esa.restclient;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;

public interface RequestContentTypeFactory extends Ordered {
    ContentType create(HttpHeaders requestHeaders, Object entity);
}
