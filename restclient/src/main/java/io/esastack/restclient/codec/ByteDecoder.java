package io.esastack.restclient.codec;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;

import java.lang.reflect.Type;

public interface ByteDecoder extends Decoder {

    @Override
    default <T> CodecResult<T> decode(MediaType mediaType, HttpHeaders headers, ResponseContent responseContent,
                                      Class<T> type, Type genericType) throws Exception {
        if (responseContent.content() instanceof byte[]) {
            return doDecode(mediaType, headers, (byte[]) responseContent.content(), type, genericType);
        }

        return CodecResult.fail();
    }

    <T> CodecResult<T> doDecode(MediaType mediaType, HttpHeaders headers, byte[] content,
                                Class<T> type, Type genericType) throws Exception;

}
