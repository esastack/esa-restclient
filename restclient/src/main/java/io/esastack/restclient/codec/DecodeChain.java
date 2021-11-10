package io.esastack.restclient.codec;

import io.esastack.commons.net.http.MediaType;

import java.lang.reflect.Type;

public interface DecodeChain {

    MediaType contentType();

    ResponseContent<?> content();

    Class<?> type();

    Type genericType();

    /**
     * Proceed to the next member in the chain.
     *
     * @return decoded object
     * @throws Exception error
     */
    Object next() throws Exception;
}
