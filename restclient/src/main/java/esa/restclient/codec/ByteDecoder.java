package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;
import esa.restclient.ResponseBodyContent;

import java.lang.reflect.Type;

public interface ByteDecoder extends Decoder {
    default <T> T decode(MediaType mediaType, HttpHeaders headers, ResponseBodyContent<?> content, Type type) throws Exception {
        if (content.type() == ResponseBodyContent.TYPE.BYTES) {
            return doDecode(mediaType, headers, (byte[]) content.content(), type);
        } else {
            throw new UnsupportedOperationException("ByteDecoder only support decode content which type is bytes");
        }
    }

    <T> T doDecode(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) throws Exception;
}
