package io.esastack.restclient.spi;

import io.esastack.restclient.codec.EncodeAdvice;
import io.esastack.restclient.spi.impl.EncodeAdviceFactoryImpl;

import java.util.Collection;

public interface EncodeAdviceFactory {
    EncodeAdviceFactory DEFAULT = new EncodeAdviceFactoryImpl();

    Collection<EncodeAdvice> encodeAdvices();
}
