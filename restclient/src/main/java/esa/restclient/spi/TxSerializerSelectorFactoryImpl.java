package esa.restclient.spi;

import esa.commons.spi.SpiLoader;
import esa.restclient.serializer.TxSerializerSelector;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TxSerializerSelectorFactoryImpl implements TxSerializerSelectorFactory {

    @Override
    public Collection<TxSerializerSelector> txSerializerSelectors() {
        List<TxSerializerSelector> txSerializerSelectors = SpiLoader.getAll(TxSerializerSelector.class);
        return txSerializerSelectors == null ? Collections.emptyList() : Collections.unmodifiableList(txSerializerSelectors);
    }

}
