package io.esastack.restclient.codec;

import io.esastack.httpclient.core.util.Ordered;

/**
 * <code>Encoder</code> is designed for the conversion from Java type to {@link RequestContent}.And
 * in many scenarios, what you need is {@link ByteEncoder} which makes it unnecessary for you to understand
 * the {@link RequestContent}.
 */
public interface Encoder extends Ordered {

    /**
     * Encode the {@code encodeContext.entity()} to {@link RequestContent}.If this encoder can encode the entity,
     * it will directly encode and return {@link RequestContent}. Otherwise, it will call {@code encodeContext.next()
     * to hand over the encoding work to the next encoder
     *
     * @param encodeContext which is to save variables required during encoding
     * @return RequestContent
     * @throws Exception error
     */
    RequestContent<?> encode(EncodeContext<?> encodeContext) throws Exception;
}
