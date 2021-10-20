package io.esastack.restclient.codec;

import io.esastack.restclient.RequestBodyContent;
import io.esastack.restclient.RestRequest;

/**
 * Context class used by {@link EncodeAdvice} to intercept calls to
 * {@link Encoder#encode}
 * The member variables in this context class correspond to the
 * parameters of the intercepted method {@link Encoder#encode}
 *
 * @see Encoder
 * @see EncodeAdvice
 */
public interface EncodeContext {

    RestRequest request();

    Object entity();

    /**
     * set entity,this method is not safe for use by multiple threads
     *
     * @param entity entity
     */
    void entity(Object entity);

    /**
     * Proceed to the next advice in the chain.
     * <p>
     * Advice MUST explicitly call this method to continue the execution chain;
     * the call to this method in the last of the chain will invoke
     * the wrapped {@link Encoder#encode} method.
     *
     * @return encoded requestBodyContent
     * @throws Exception error
     */
    RequestBodyContent<?> proceed() throws Exception;
}
