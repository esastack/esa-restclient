package io.esastack.restclient.codec;

import io.esastack.httpclient.core.util.Ordered;

/**
 * Interface for decode advice that wrap around calls to {@link Decoder#decode}.
 *
 * @see Decoder
 */
public interface DecodeAdvice extends Ordered {

    /**
     * Method wrapping calls to {@link Decoder#decode}. method.
     * <p>
     * The parameters of the wrapped method called are available from context. Implementations
     * of this method SHOULD explicitly call {@link DecodeAdviceContext#proceed()} to invoke the
     * next <code>DecodeAdvice</code> in the chain, and ultimately the wrapped
     * {@link Decoder#decode}. method.
     *
     * @param context decode invocation context
     * @return decoded object
     * @throws Exception error
     * @see Decoder
     * @see DecodeAdviceContext
     */
    Object aroundDecode(DecodeAdviceContext context) throws Exception;
}
