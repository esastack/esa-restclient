package io.esastack.restclient.codec;

import io.esastack.httpclient.core.util.Ordered;

/**
 * <code>Decoder</code> is designed for the conversion from {@code decodeContext.content()} to object.And
 * in many scenarios, what you need is {@link ByteDecoder} which makes it unnecessary for you to understand
 * the {@link ResponseContent}.
 */
public interface Decoder<V> extends Ordered {

    /**
     * Decode the {@code decodeContext.content()} to object.If this decoder can decode the {@code decodeContext.content()},
     * it will directly decode and return object.Otherwise, it will call {@code decodeContext.next()} to hand over the
     * decoding work to the next decoder.
     *
     * @param decodeContext which is to save variables required during decoding
     * @return object
     * @throws Exception error
     */
    Object decode(DecodeContext<V> decodeContext) throws Exception;
}
