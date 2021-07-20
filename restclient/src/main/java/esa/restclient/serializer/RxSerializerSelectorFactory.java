package esa.restclient.serializer;

import java.util.Collection;

public interface RxSerializerSelectorFactory {

    RxSerializerSelectorFactory DEFAULT = new RxSerializerSelectorFactoryImpl();

    Collection<RxSerializerSelector> rxSerializerSelectors();
}
