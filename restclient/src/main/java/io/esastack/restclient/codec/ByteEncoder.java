package io.esastack.restclient.codec;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;

import java.lang.reflect.Type;

public interface ByteEncoder extends Encoder {

    @Override
    default CodecResult<RequestContent> encode(MediaType mediaType, HttpHeaders headers,
                                                   Object entity, Class<?> type, Type genericType) throws Exception {

        CodecResult<byte[]> codecResult = doEncode(mediaType, headers, entity, type, genericType);
        if (codecResult.isSuccess()) {
            return CodecResult.success(RequestContent.of(codecResult.getResult()));
        }
        return CodecResult.fail();
    }

    CodecResult<byte[]> doEncode(MediaType mediaType, HttpHeaders headers,
                                 Object entity, Class<?> type, Type genericType) throws Exception;
}
