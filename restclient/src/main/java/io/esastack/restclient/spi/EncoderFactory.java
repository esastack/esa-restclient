package io.esastack.restclient.spi;

import esa.commons.spi.SPI;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.codec.Encoder;

import java.util.Collection;

@SPI
public interface EncoderFactory {
    Collection<Encoder<?>> encoders(RestClientOptions clientOptions);
}
