package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public interface BodyProcessor {

    Object read(
            Class rawType,
            Type type,
            MediaType mediaType,
            HttpHeaders httpHeaders,
            InputStream bodyStream);

    void write(Object entity,
               Type type,
               MediaType mediaType,
               HttpHeaders httpHeaders,
               OutputStream bodyStream);
}
