package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;
import esa.restclient.RequestBodyContent;

public interface ByteEncoder extends Encoder {
    @Override
    default RequestBodyContent<byte[]> encode(MediaType mediaType,
                                              HttpHeaders headers, Object entity) throws Exception {
        return RequestBodyContent.of(doEncode(mediaType, headers, entity));
    }

    byte[] doEncode(MediaType mediaType, HttpHeaders headers, Object entity) throws Exception;
}
