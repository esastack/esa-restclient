package esa.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.RestRequest;
import esa.restclient.codec.ByteDecoder;
import esa.restclient.codec.ByteDecoderSelector;
import io.esastack.commons.net.http.MediaType;

import java.lang.reflect.Type;

public class StringDecoderSelector extends ByteDecoderSelector {

    private static final StringCodec CODEC = new StringCodec();

    @Override
    public ByteDecoder doSelect(RestRequest request, ContentType[] acceptTypes, Type type,
                                MediaType responseMediaType, HttpHeaders responseHeaders) {
        if (String.class.equals(type)
        ) {
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
