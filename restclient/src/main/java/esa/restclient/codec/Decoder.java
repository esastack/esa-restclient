package esa.restclient.codec;

import esa.commons.annotation.Internal;
import esa.commons.http.HttpHeaders;
import esa.restclient.BodyContent;
import esa.restclient.MediaType;

import java.lang.reflect.Type;

@Internal
public interface Decoder {

    <T> T decode(MediaType mediaType, HttpHeaders headers, BodyContent<?> content, Type type) throws Exception;
}
