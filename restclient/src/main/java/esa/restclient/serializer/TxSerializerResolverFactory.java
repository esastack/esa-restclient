package esa.restclient.serializer;

import java.util.Collection;

public interface TxSerializerResolverFactory {
    TxSerializerResolverFactory DEFAULT = new TxSerializerResolverFactoryImpl();

    Collection<TxSerializerResolver> txSerializerResolvers();
}
