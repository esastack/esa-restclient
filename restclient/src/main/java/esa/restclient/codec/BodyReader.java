package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;
import esa.restclient.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

public interface BodyReader<T> extends Ordered {

    boolean canRead(Class rawType, Type type, MediaType mediaType, HttpHeaders httpHeaders);

    T read(
            Class rawType,
            Type type,
            MediaType mediaType,
            HttpHeaders httpHeaders,
            InputStream bodyStream) throws IOException;
}
