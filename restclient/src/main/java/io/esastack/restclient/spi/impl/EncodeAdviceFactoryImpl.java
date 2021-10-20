package io.esastack.restclient.spi.impl;

import esa.commons.spi.SpiLoader;
import io.esastack.restclient.codec.EncodeAdvice;
import io.esastack.restclient.spi.EncodeAdviceFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EncodeAdviceFactoryImpl implements EncodeAdviceFactory {
    @Override
    public Collection<EncodeAdvice> encodeAdvices() {
        List<EncodeAdvice> encodeAdvices = SpiLoader.getAll(EncodeAdvice.class);
        return encodeAdvices == null
                ? Collections.emptyList() : Collections.unmodifiableList(encodeAdvices);
    }
}
