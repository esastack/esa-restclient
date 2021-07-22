package esa.restclient.spi;

import esa.commons.spi.SpiLoader;
import esa.restclient.codec.DecoderSelector;

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
