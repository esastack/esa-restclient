package esa.restclient.core.codec;

import esa.commons.http.HttpHeaders;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.util.Ordered;
import esa.restclient.core.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public interface BodyWriter<T> extends Ordered {

    boolean canWrite(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders);

    void write(T entity,
               Type genericType,
               MediaType mediaType,
               HttpHeaders httpHeaders,
               OutputStream bodyStream) throws IOException;
}
