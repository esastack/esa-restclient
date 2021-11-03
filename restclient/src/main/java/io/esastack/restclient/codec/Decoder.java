package io.esastack.restclient.codec;

import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.util.Ordered;

import java.lang.reflect.Type;

/**
 * <code>Decoder</code> is designed for the conversion from {@code bodyContent} to {@link CodecResult}.
 *
 * @see CodecResult
 */
public interface Decoder extends Ordered {

    /**
     * Decode the bodyContent to {@link CodecResult}.The call of {@code CodecResult.isSuccess()} will return false
     * when the Decoder can,t decode the bodyContent,otherwise it will return true,and the decoded result
     * can be get by {@code CodecResult.getResult()}
     *
     * @param mediaType    the media type of the HTTP response
     * @param headers      the headers of the HTTP response
     * @param responseBody the body of the HTTP response
     * @param type         the class will be decoded from the bodyContent.
     * @param genericType  the genericType will be decoded from the bodyContent.
     * @return decoded result
     * @throws Exception error
     */
    <T> CodecResult<T> decode(MediaType mediaType, HttpHeaders headers, ResponseBody<?> responseBody,
                          Class<T> type, Type genericType) throws Exception;
}
