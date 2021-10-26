package io.esastack.restclient.codec;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.ResponseBodyContent;

import java.lang.reflect.Type;

public interface ByteDecoder extends Decoder {
    default <T> T decode(MediaType mediaType, HttpHeaders headers,
                         ResponseBodyContent<?> content, Type type) throws Exception {
        Checks.checkNotNull(content, "content");
        return doDecode(mediaType, headers, (byte[]) content.content(), type);
    }

    <T> T doDecode(MediaType mediaType, HttpHeaders headers, byte[] data, Type type) throws Exception;
}
