package io.esastack.restclient.codec;

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

    ResponseContent responseContent();

    /**
     * set responseContent,this method is not safe for use by multiple threads
     *
     * @param responseContent responseContent
     */
    void responseContent(ResponseContent responseContent);

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
