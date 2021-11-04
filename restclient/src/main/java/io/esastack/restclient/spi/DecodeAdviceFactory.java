package io.esastack.restclient.spi;

import esa.commons.spi.SPI;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.codec.DecodeAdvice;

import java.util.Collection;

@SPI
public interface DecodeAdviceFactory {
    Collection<DecodeAdvice> decodeAdvices(RestClientOptions clientOptions);
}
