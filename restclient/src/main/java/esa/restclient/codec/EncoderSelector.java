package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;
import esa.restclient.ContentType;

public interface EncoderSelector extends Ordered {
    Encoder<?> select(HttpHeaders requestHeaders, ContentType contentType, Object entity);

    @Override
    default int getOrder() {
        return MIDDLE_PRECEDENCE;
    }
}
