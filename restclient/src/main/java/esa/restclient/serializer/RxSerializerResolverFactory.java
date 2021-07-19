package esa.restclient.serializer;

import java.util.Collection;

public interface RxSerializerResolverFactory {

    RxSerializerResolverFactory DEFAULT = new RxSerializerResolverFactoryImpl();

    Collection<RxSerializerResolver> rxSerializerResolvers();
}
