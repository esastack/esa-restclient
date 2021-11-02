package io.esastack.restclient.codec;

import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;

import java.lang.reflect.Type;

/**
 * Context class used by {@link DecodeAdvice} to intercept calls to
 * {@link Decoder#decode}.
 * The member variables in this context class correspond to the
 * parameters of the intercepted method {@link Decoder#decode}
 *
 * @see Decoder
 * @see DecodeAdvice
 */
public interface DecodeContext {

    RestRequest request();

    RestResponse response();

    /**
     * @return contentType of response
     */
    MediaType contentType();

    /**
     * Update contentType of response.this method is not safe for
     * use by multiple threads
     *
     * @param mediaType new contentType for response
     */
    void contentType(MediaType mediaType);

    ResponseBody<?> responseBody();

    /**
     * set responseBody,this method is not safe for use by multiple threads
     *
     * @param responseBody responseBody
     */
    void responseBody(ResponseBody<?> responseBody);

    Class<?> type();

    Type genericType();

    /**
     * Proceed to the next advice in the chain.
     * <p>
     * Advice MUST explicitly call this method to continue the execution chain;
     * the call to this method in the last of the chain will invoke
     * the wrapped {@link Decoder#decode} method.
     *
     * @return decoded object
     * @throws Exception error
     */
    Object proceed() throws Exception;
}
