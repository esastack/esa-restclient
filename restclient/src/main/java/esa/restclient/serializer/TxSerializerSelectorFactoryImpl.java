package esa.restclient.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TxSerializerSelectorFactoryImpl implements TxSerializerSelectorFactory {

    @Override
    public Collection<TxSerializerSelector> txSerializerSelectors() {
        List<TxSerializerSelector> txSerializerSelectors = new ArrayList<>();
        txSerializerSelectors.add(DefaultSerializerSelector.INSTANCE);
        return txSerializerSelectors;
    }

}
