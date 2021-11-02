package io.esastack.restclient.codec;

import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.util.Ordered;

import java.lang.reflect.Type;

/**
 * <code>Encoder</code> is designed for the conversion from Java type to {@link EncodeResult}.
 *
 * @see EncodeResult
 */
public interface Encoder extends Ordered {

    /**
     * Encode the object to {@link EncodeResult}.The call of {@code EncodeResult.isSuccess()} will return false
     * when the Encoder can,t encode the entity,otherwise it will return true,and the encoded result
     * can be get by {@code EncodeResult.getResult()}
     *
     * @param mediaType   mediaType the media type of the HTTP request
     * @param headers     headers the headers of the HTTP request
     * @param entity      the entity need to be encode
     * @param type        the class of entity
     * @param genericType the genericType of entity
     * @return {@link EncodeResult}
     * @throws Exception error
     */
    EncodeResult encode(MediaType mediaType, HttpHeaders headers, Object entity,
                           Class<?> type, Type genericType) throws Exception;
}
