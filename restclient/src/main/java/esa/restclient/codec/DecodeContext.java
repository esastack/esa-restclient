package esa.restclient.codec;

import esa.restclient.ResponseBodyContent;
import esa.restclient.RestRequest;
import esa.restclient.RestResponse;
import io.esastack.commons.net.http.MediaType;

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

    MediaType mediaType();

    /**
     * Update media type of HTTP entity.this method is not safe for
     * use by multiple threads
     *
     * @param mediaType new type for HTTP entity
     */
    void mediaType(MediaType mediaType);

    ResponseBodyContent<?> content();

    /**
     * set content,this method is not safe for use by multiple threads
     *
     * @param content content
     */
    void content(ResponseBodyContent<?> content);

    Type type();

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
