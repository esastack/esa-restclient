package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;
import esa.restclient.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

public interface BodyReader<T> extends Ordered {

    boolean canRead(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders);

    T read(
            Class type,
            Type genericType,
            MediaType mediaType,
            HttpHeaders httpHeaders,
            InputStream bodyStream) throws IOException;
}
