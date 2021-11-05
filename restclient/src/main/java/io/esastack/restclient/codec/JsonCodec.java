package io.esastack.restclient.codec;

import esa.commons.DateUtils;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;

import java.lang.reflect.Type;

public interface JsonCodec extends Encoder, Decoder {
    String DEFAULT_DATE_FORMAT = DateUtils.yyyyMMddHHmmss;

    @Override
    default <T> CodecResult<T> decode(MediaType mediaType, HttpHeaders headers, ResponseBody responseBody,
                                      Class<T> type, Type genericType) throws Exception {
        if (responseBody.isBytes() && mediaType != null
                && MediaTypeUtil.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            byte[] body = (byte[]) responseBody.content();
            if (body == null) {
                return CodecResult.success(null);
            }
            return CodecResult.success(doDecode(mediaType, headers, body, type, genericType));
        }
        return CodecResult.fail();
    }

    <T> T doDecode(MediaType mediaType, HttpHeaders headers, byte[] body,
                   Class<T> type, Type genericType) throws Exception;

    @Override
    default CodecResult<RequestBody> encode(MediaType mediaType, HttpHeaders headers, Object entity,
                                               Class<?> type, Type genericType) throws Exception {
        if (mediaType != null && MediaTypeUtil.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            return CodecResult.success(RequestBody.of(
                    doEncode(mediaType, headers, entity, type, genericType)));
        }

        return CodecResult.fail();
    }

    byte[] doEncode(MediaType mediaType, HttpHeaders headers, Object entity,
                    Class<?> type, Type genericType) throws Exception;
}
