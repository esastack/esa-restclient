package esa.restclient.serializer;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;
import esa.restclient.ContentType;

public interface TxSerializerSelector extends Ordered {
    TxSerializer select(HttpHeaders requestHeaders, ContentType contentType, Object entity);
}
