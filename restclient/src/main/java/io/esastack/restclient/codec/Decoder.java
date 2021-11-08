package io.esastack.restclient.codec;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.util.Ordered;

import java.lang.reflect.Type;

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
    <T> CodecResult<T> decode(MediaType mediaType, HttpHeaders headers, ResponseContent responseContent,
                              Class<T> type, Type genericType) throws Exception;
}
