package esa.restclient.core.codec;

import esa.commons.http.HttpHeaders;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.util.Ordered;
import esa.restclient.core.MediaType;

import java.lang.reflect.Type;

public interface Encoder<T> extends Ordered {

    boolean canEncode(Class<?> type, Type genericType, MediaType mediaType);

    Buffer encode(T entity,
                  Type genericType,
                  MediaType mediaType,
                  HttpHeaders httpHeaders);
}
