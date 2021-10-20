package io.esastack.restclient.codec.impl;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.ContentType;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.codec.ByteDecoder;
import io.esastack.restclient.codec.ByteDecoderSelector;

import java.lang.reflect.Type;

public class ProtoBufDecoderSelector extends ByteDecoderSelector {
    private final ProtoBufCodec codec;

    public ProtoBufDecoderSelector() {
        this.codec = new ProtoBufCodec();
    }

    public ProtoBufDecoderSelector(ProtoBufCodec codec) {
        Checks.checkNotNull(codec, "codec");
        this.codec = codec;
    }

    @Override
    public ByteDecoder doSelect(RestRequest request, ContentType[] acceptTypes, Type type,
                                MediaType responseMediaType, HttpHeaders responseHeaders) {
        if (ContentType.PROTOBUF.mediaType().isCompatibleWith(responseMediaType)) {
            return codec;
        }
        return null;
    }

    @Override
    public int getOrder() {
        return LOWER_PRECEDENCE;
    }
}
