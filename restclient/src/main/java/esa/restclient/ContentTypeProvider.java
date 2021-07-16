package esa.restclient;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;


public interface ContentTypeProvider extends Ordered {
    ContentType offer(HttpHeaders requestHeaders, Object entity);
}
