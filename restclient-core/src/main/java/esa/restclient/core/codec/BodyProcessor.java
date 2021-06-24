package esa.restclient.core.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.core.MediaType;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public interface BodyProcessor {

    Object read(
            Class type,
            Type genericType,
            MediaType mediaType,
            HttpHeaders httpHeaders,
            InputStream bodyStream);

    void write(Object entity,
               Type genericType,
               MediaType mediaType,
               HttpHeaders httpHeaders,
               OutputStream bodyStream);
}
