package esa.restclient.spi;

import esa.commons.spi.SpiLoader;
import esa.restclient.serializer.RxSerializerSelector;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RxSerializerSelectorFactoryImpl implements RxSerializerSelectorFactory {

    @Override
    public Collection<RxSerializerSelector> rxSerializerSelectors() {
        List<RxSerializerSelector> rxSerializerSelectors = SpiLoader.getAll(RxSerializerSelector.class);
        return rxSerializerSelectors == null
                ? Collections.emptyList() : Collections.unmodifiableList(rxSerializerSelectors);
    }

}
