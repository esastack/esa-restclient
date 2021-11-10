package io.esastack.restclient.codec;


import io.esastack.commons.net.http.MediaType;

import java.lang.reflect.Type;

public interface EncodeChain {

    MediaType contentType();

    Object entity();

    Class<?> type();

    Type genericType();

    /**
     * Proceed to the next member in the chain.
     *
     * @return encoded requestContent
     * @throws Exception error
     */
    RequestContent<?> next() throws Exception;
}
