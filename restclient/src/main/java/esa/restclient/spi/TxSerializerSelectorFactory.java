package esa.restclient.spi;

import esa.restclient.serializer.TxSerializerSelector;

import java.util.Collection;

public interface TxSerializerSelectorFactory {
    TxSerializerSelectorFactory DEFAULT = new TxSerializerSelectorFactoryImpl();

    Collection<TxSerializerSelector> txSerializerSelectors();
}
