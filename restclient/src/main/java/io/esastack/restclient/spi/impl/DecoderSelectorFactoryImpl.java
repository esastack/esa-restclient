package io.esastack.restclient.spi.impl;

import esa.commons.spi.SpiLoader;
import io.esastack.restclient.codec.DecoderSelector;
import io.esastack.restclient.spi.DecoderSelectorFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DecoderSelectorFactoryImpl implements DecoderSelectorFactory {

    @Override
    public Collection<DecoderSelector> decoderSelectors() {
        List<DecoderSelector> decoderSelectors = SpiLoader.getAll(DecoderSelector.class);
        return decoderSelectors == null
                ? Collections.emptyList() : Collections.unmodifiableList(decoderSelectors);
    }
}
