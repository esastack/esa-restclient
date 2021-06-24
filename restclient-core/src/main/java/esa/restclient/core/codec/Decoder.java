package esa.restclient.core.codec;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;
import esa.restclient.core.MediaType;

import java.io.InputStream;
import java.lang.reflect.Type;

public interface Decoder extends Ordered {

    boolean canDecode(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders);

    Object decode(
            Class type,
            Type genericType,
            MediaType mediaType,
            HttpHeaders httpHeaders,
            InputStream entityStream);
}
