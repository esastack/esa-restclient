package esa.restclient.serializer;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;
import esa.restclient.ContentType;

public interface TxSerializerResolver extends Ordered {
    TxSerializer resolve(HttpHeaders requestHeaders, ContentType contentType, Object entity);
}
