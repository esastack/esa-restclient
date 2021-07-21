package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

public class ByteToByteDecoderSelector extends ByteDecoderSelector {
    private final static ByteToByteCodec CODEC = new ByteToByteCodec();

    public ByteToByteDecoderSelector() {
    }

    @Override
    public ByteDecoder doSelect(RestRequest request, ContentType[] acceptTypes, Type type,
                                MediaType responseMediaType, HttpHeaders responseHeaders) {
        if (byte[].class.equals(type)) {
            return CODEC;
        } else {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return LOWER_PRECEDENCE;
    }
}
