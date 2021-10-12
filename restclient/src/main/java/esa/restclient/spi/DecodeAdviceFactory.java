package esa.restclient.spi;

import esa.restclient.codec.DecodeAdvice;
import esa.restclient.spi.impl.DecodeAdviceFactoryImpl;

import java.util.Collection;

public interface DecodeAdviceFactory {
    DecodeAdviceFactory DEFAULT = new DecodeAdviceFactoryImpl();

    Collection<DecodeAdvice> decodeAdvices();
}
