package esa.restclient.core.codec;

import esa.commons.http.HttpHeaders;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.util.Ordered;
import esa.restclient.core.MediaType;

import java.lang.reflect.Type;

public interface Decoder<T> extends Ordered {

    boolean canDecode(Class<?> type, Type genericType, MediaType mediaType);

    T decode(
            Class<T> type,
            Type genericType,
            MediaType mediaType,
            HttpHeaders httpHeaders,
            Buffer buffer);
}
