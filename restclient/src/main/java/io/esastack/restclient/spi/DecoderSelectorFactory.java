package io.esastack.restclient.spi;

import io.esastack.restclient.codec.DecoderSelector;
import io.esastack.restclient.spi.impl.DecoderSelectorFactoryImpl;

import java.util.Collection;

public interface DecoderSelectorFactory {
    DecoderSelectorFactory DEFAULT = new DecoderSelectorFactoryImpl();

    Collection<DecoderSelector> decoderSelectors();
}
