package esa.restclient.serializer;

import java.util.Collection;

public interface TxSerializerSelectorFactory {
    TxSerializerSelectorFactory DEFAULT = new TxSerializerSelectorFactoryImpl();

    Collection<TxSerializerSelector> txSerializerSelectors();
}
