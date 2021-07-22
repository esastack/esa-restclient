package esa.restclient.spi;

import esa.commons.spi.SpiLoader;
import esa.restclient.codec.DecodeAdvice;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DecodeAdviceFactoryImpl implements DecodeAdviceFactory {
    @Override
    public Collection<DecodeAdvice> decodeAdvices() {
        List<DecodeAdvice> decodeAdvices = SpiLoader.getAll(DecodeAdvice.class);
        return decodeAdvices == null
                ? Collections.emptyList() : Collections.unmodifiableList(decodeAdvices);
    }
}
