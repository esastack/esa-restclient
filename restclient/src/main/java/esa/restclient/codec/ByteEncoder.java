package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.BodyContent;
import esa.restclient.MediaType;

public interface ByteEncoder extends Encoder {
    @Override
    default BodyContent<byte[]> encode(MediaType mediaType, HttpHeaders headers, Object entity) throws Exception {
        return BodyContent.of(doEncode(mediaType, headers, entity));
    }

    byte[] doEncode(MediaType mediaType, HttpHeaders headers, Object entity) throws Exception;
}
