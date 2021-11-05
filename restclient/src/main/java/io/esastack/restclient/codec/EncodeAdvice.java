package io.esastack.restclient.codec;

import io.esastack.httpclient.core.util.Ordered;

/**
 * Interface for encode advice that wrap around calls to {@link Encoder#encode}
 *
 * @see Encoder
 */
public interface EncodeAdvice extends Ordered {

    /**
     * Method wrapping calls to {@link Encoder#encode} method.
     * <p>
     * The parameters of the wrapped method called are available from context. Implementations
     * of this method SHOULD explicitly call {@link EncodeContext#proceed()} to invoke the
     * next <code>EncodeAdvice</code> in the chain, and ultimately the wrapped {@link
     * Encoder#encode} method.
     *
     * @param context encode invocation context
     * @return encoded requestBody
     * @throws Exception error
     * @see Encoder
     * @see EncodeContext
     */
    RequestBody aroundEncode(EncodeContext context) throws Exception;
}
