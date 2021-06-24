package esa.restclient.core.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.core.MediaType;

import java.io.InputStream;
import java.lang.reflect.Type;

public interface CodecManager {

    Object decode(
            Class type,
            Type genericType,
            MediaType mediaType,
            HttpHeaders httpHeaders,
            InputStream entityStream);

    InputStream encode(Object entity,
                       Type genericType,
                       MediaType mediaType,
                       HttpHeaders httpHeaders);
}
