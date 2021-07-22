package esa.restclient.spi;

import esa.restclient.codec.DecoderSelector;

import java.util.Collection;

public interface DecoderSelectorFactory {
    DecoderSelectorFactory DEFAULT = new DecoderSelectorFactoryImpl();

    Collection<DecoderSelector> decoderSelectors();
}
