package io.esastack.restclient.codec;

import io.esastack.restclient.RestRequest;

import java.lang.reflect.Type;

/**
 * Context class used by {@link EncodeAdvice} to intercept the call of
 * {@link Encoder#encode}.
 * The member variables in this context class correspond to the
 * parameters of the intercepted method {@link Encoder#encode}
 *
 * @see Encoder
 * @see EncodeAdvice
 */
public interface EncodeAdviceContext extends EncodeChain {

    RestRequest request();

    /**
     * set entity,this method is not safe for use by multiple threads
     *
     * @param entity entity
     */
    void entity(Object entity);

    /**
     * set entity and genericType,this method is not safe for use by multiple threads
     *
     * @param entity      entity
     * @param genericType genericType
     */
    void entity(Object entity, Type genericType);
}
