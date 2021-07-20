package esa.restclient.spi;

import esa.restclient.serializer.RxSerializerSelector;

import java.util.Collection;

public interface RxSerializerSelectorFactory {

    RxSerializerSelectorFactory DEFAULT = new RxSerializerSelectorFactoryImpl();

    Collection<RxSerializerSelector> rxSerializerSelectors();
}
