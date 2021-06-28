package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;
import esa.restclient.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

public interface BodyWriter<T> extends Ordered {

    boolean canWrite(Class rawType, Type type, MediaType mediaType, HttpHeaders httpHeaders);

    void write(T entity,
               Type type,
               MediaType mediaType,
               HttpHeaders httpHeaders,
               OutputStream bodyStream) throws IOException;
}
