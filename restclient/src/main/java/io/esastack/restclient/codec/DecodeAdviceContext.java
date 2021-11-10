package io.esastack.restclient.codec;

import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;

/**
 * Context class used by {@link DecodeAdvice} to intercept calls to
 * {@link Decoder#decode}.
 * The member variables in this context class correspond to the
 * parameters of the intercepted method {@link Decoder#decode}
 *
 * @see Decoder
 * @see DecodeAdvice
 */
public interface DecodeAdviceContext extends DecodeChain {

    RestRequest request();

    RestResponse response();

    /**
     * set responseContent,this method is not safe for use by multiple threads
     *
     * @param responseContent responseContent
     */
    void content(ResponseContent<?> responseContent);
}
