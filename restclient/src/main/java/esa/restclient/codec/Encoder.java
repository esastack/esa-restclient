package esa.restclient.codec;

import esa.commons.annotation.Internal;
import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;

@Internal
public interface Encoder<T> {
    T encode(MediaType mediaType, HttpHeaders headers, Object entity) throws Exception;
}
