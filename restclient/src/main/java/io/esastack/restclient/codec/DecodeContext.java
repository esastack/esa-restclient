package io.esastack.restclient.codec;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;

import java.lang.reflect.Type;

public interface DecodeContext<V> {

    MediaType contentType();

    HttpHeaders headers();

    ResponseContent<V> content();

    Class<?> type();

    Type genericType();

    Object next() throws Exception;
}
