package io.esastack.restclient.spi;

import io.esastack.restclient.codec.DecodeAdvice;
import io.esastack.restclient.spi.impl.DecodeAdviceFactoryImpl;

import java.util.Collection;

public interface DecodeAdviceFactory {
    DecodeAdviceFactory DEFAULT = new DecodeAdviceFactoryImpl();

    Collection<DecodeAdvice> decodeAdvices();
}
