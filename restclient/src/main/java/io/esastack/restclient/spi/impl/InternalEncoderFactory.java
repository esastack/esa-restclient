package io.esastack.restclient.spi.impl;

import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.impl.ByteToByteCodec;
import io.esastack.restclient.codec.impl.FileEncoder;
import io.esastack.restclient.codec.impl.FormURLEncodedEncoder;
import io.esastack.restclient.codec.impl.JacksonCodec;
import io.esastack.restclient.codec.impl.MultipartEncoder;
import io.esastack.restclient.codec.impl.StringCodec;
import io.esastack.restclient.spi.EncoderFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class InternalEncoderFactory implements EncoderFactory {

    @Override
    public Collection<Encoder<?>> encoders(RestClientOptions clientOptions) {
        List<Encoder<?>> encoders = new ArrayList<>();
        encoders.add(new ByteToByteCodec());
        encoders.add(new FileEncoder());
        encoders.add(new FormURLEncodedEncoder());
        encoders.add(new JacksonCodec());
        encoders.add(new MultipartEncoder());
        encoders.add(new StringCodec());
        return encoders;
    }
}
