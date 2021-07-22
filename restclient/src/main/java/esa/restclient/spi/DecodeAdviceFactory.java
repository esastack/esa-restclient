package esa.restclient.spi;

import esa.restclient.codec.DecodeAdvice;

import java.util.Collection;

public interface DecodeAdviceFactory {
    DecodeAdviceFactory DEFAULT = new DecodeAdviceFactoryImpl();

    Collection<DecodeAdvice> decodeAdvices();
}
