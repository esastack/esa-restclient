package io.esastack.restclient.spi;

import esa.commons.spi.SPI;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.codec.EncodeAdvice;

import java.util.Collection;

@SPI
public interface EncodeAdviceFactory {
    Collection<EncodeAdvice> encodeAdvices(RestClientOptions clientOptions);
}
