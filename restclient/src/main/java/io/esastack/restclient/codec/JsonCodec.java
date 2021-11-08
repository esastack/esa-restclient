package io.esastack.restclient.codec;

import esa.commons.DateUtils;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;

import java.lang.reflect.Type;

public interface JsonCodec extends ByteCodec {
    String DEFAULT_DATE_FORMAT = DateUtils.yyyyMMddHHmmss;

    @Override
    default <T> CodecResult<T> doDecode(MediaType mediaType, HttpHeaders headers, byte[] content,
                                        Class<T> type, Type genericType) throws Exception {
        if (mediaType != null && MediaTypeUtil.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            if (content == null) {
                return CodecResult.success(null);
            }
            return CodecResult.success(decodeFromJson(mediaType, headers, content, type, genericType));
        }
        return CodecResult.fail();
    }

    <T> T decodeFromJson(MediaType mediaType, HttpHeaders headers, byte[] jsonContent,
                         Class<T> type, Type genericType) throws Exception;

    @Override
    default CodecResult<byte[]> doEncode(MediaType mediaType, HttpHeaders headers, Object entity,
                                         Class<?> type, Type genericType) throws Exception {
        if (mediaType != null && MediaTypeUtil.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            return CodecResult.success(encodeToJson(mediaType, headers, entity, type, genericType));
        }

        return CodecResult.fail();
    }

    byte[] encodeToJson(MediaType mediaType, HttpHeaders headers, Object entity,
                        Class<?> type, Type genericType) throws Exception;
}
