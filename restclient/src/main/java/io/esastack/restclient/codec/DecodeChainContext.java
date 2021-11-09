package io.esastack.restclient.codec;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;

import java.lang.reflect.Type;

public interface DecodeChainContext<T> {

    MediaType contentType();

    HttpHeaders headers();

    ResponseContent responseContent();

    Class<T> type();

    Type genericType();

    T continueToDecode() throws Exception;
}
