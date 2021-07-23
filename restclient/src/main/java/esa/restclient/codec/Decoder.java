package esa.restclient.codec;

import esa.commons.annotation.Internal;
import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;
import esa.restclient.ResponseBodyContent;

import java.lang.reflect.Type;

@Internal
public interface Decoder {

    <T> T decode(MediaType mediaType, HttpHeaders headers, ResponseBodyContent<?> content, Type type) throws Exception;
}
