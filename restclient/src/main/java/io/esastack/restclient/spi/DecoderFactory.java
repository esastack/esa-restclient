package io.esastack.restclient.spi;

import esa.commons.spi.SPI;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.codec.Decoder;

import java.util.Collection;

@SPI
public interface DecoderFactory {
    Collection<Decoder> decoders(RestClientOptions clientOptions);
}
