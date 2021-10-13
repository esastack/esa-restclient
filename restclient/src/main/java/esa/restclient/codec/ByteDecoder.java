package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.ResponseBodyContent;
import io.esastack.commons.net.http.MediaType;

import java.lang.reflect.Type;

public interface ByteDecoder extends Decoder {
    default <T> T decode(MediaType mediaType, HttpHeaders headers,
                         ResponseBodyContent<?> content, Type type) throws Exception {
        return doDecode(mediaType, headers, (byte[]) content.content(), type);
    }

    <T> T doDecode(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) throws Exception;
}
