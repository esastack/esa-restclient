package io.esastack.restclient.codec;

import io.esastack.httpclient.core.util.Ordered;

/**
 * <code>Encoder</code> is designed for the conversion from Java type to {@link CodecResult}.And
 * in many scenarios, what you need is {@link ByteEncoder}
 *
 * @see CodecResult
 */
public interface Encoder extends Ordered {

    /**
     * Encode the object to {@link CodecResult}.The call of {@code CodecResult.isSuccess()} will return false
     * when the Encoder can,t encode the entity,otherwise it will return true,and the encoded result
     * can be get by {@code CodecResult.getResult()}
     *
     * @param mediaType   mediaType the media type of the HTTP request
     * @param headers     headers the headers of the HTTP request
     * @param entity      the entity need to be encode
     * @param type        the class of entity
     * @param genericType the genericType of entity
     * @return encoded result
     * @throws Exception error
     */
    RequestContent encode(EncodeChainContext encodeChainContext) throws Exception;
}
