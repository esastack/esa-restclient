package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.BodyContent;
import esa.restclient.MediaType;

import java.lang.reflect.Type;

public interface ByteDecoder extends Decoder {
    default <T> T decode(MediaType mediaType, HttpHeaders headers, BodyContent<?> content, Type type) throws Exception {
        if (content.type() == BodyContent.TYPE.BYTES) {
            return doDecode(mediaType, headers, (byte[]) content.content(), type);
        } else {
            throw new UnsupportedOperationException("ByteDecoder only support decode content which type is bytes");
        }
    }

    <T> T doDecode(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) throws Exception;
}
