package esa.restclient.codec;

import esa.commons.annotation.Internal;
import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;

import java.lang.reflect.Type;

@Internal
public interface Decoder<T> {
    <U> U decode(MediaType mediaType, HttpHeaders headers, T data, Type type) throws Exception;
}
