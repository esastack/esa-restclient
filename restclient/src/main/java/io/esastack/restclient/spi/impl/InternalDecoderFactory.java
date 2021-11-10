package io.esastack.restclient.spi.impl;

import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.impl.ByteToByteCodec;
import io.esastack.restclient.codec.impl.JacksonCodec;
import io.esastack.restclient.codec.impl.StringCodec;
import io.esastack.restclient.spi.DecoderFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class InternalDecoderFactory implements DecoderFactory {

    @Override
    public Collection<Decoder<?>> decoders(RestClientOptions clientOptions) {
        List<Decoder<?>> decoders = new ArrayList<>();
        decoders.add(new ByteToByteCodec());
        decoders.add(new JacksonCodec());
        decoders.add(new StringCodec());
        return decoders;
    }
}
