package io.esastack.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.ContentType;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.codec.ByteDecoder;
import io.esastack.restclient.codec.ByteDecoderSelector;

import java.lang.reflect.Type;

public class ByteToByteDecoderSelector extends ByteDecoderSelector {
    private static final ByteToByteCodec CODEC = new ByteToByteCodec();

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
