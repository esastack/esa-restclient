package io.esastack.restclient.codec;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;

import java.lang.reflect.Type;

public interface EncodeContext<V> {

    MediaType contentType();

    HttpHeaders headers();

    Object entity();

    Class<?> type();

    Type genericType();

    RequestContent<V> next() throws Exception;
}
