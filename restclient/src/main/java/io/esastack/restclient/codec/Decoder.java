package io.esastack.restclient.codec;

import io.esastack.httpclient.core.util.Ordered;

/**
 * <code>Decoder</code> is designed for the conversion from {@code bodyContent} to {@link CodecResult}.And
 * in many scenarios, what you need is {@link ByteDecoder} which makes it unnecessary for you to understand
 * the {@link ResponseContent}.
 *
 * @see CodecResult
 */
public interface Decoder extends Ordered {

    /**
     * Decode the bodyContent to {@link CodecResult}.The call of {@code CodecResult.isSuccess()} will return false
     * when the Decoder can,t decode the bodyContent,otherwise it will return true,and the decoded result
     * can be get by {@code CodecResult.getResult()}
     *
     * @param mediaType       the media type of the HTTP response
     * @param headers         the headers of the HTTP response
     * @param responseContent the body of the HTTP response
     * @param type            the class will be decoded from the bodyContent.
     * @param genericType     the genericType will be decoded from the bodyContent.
     * @return decoded result
     * @throws Exception error
     */
    <T> CodecResult<T> decode(DecodeContext<T> decodeChainContext) throws Exception;
}
