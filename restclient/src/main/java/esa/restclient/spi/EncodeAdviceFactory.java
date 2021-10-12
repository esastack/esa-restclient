package esa.restclient.spi;

import esa.restclient.codec.EncodeAdvice;
import esa.restclient.spi.impl.EncodeAdviceFactoryImpl;

import java.util.Collection;

public interface EncodeAdviceFactory {
    EncodeAdviceFactory DEFAULT = new EncodeAdviceFactoryImpl();

    Collection<EncodeAdvice> encodeAdvices();
}
