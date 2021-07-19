package esa.restclient.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TxSerializerResolverFactoryImpl implements TxSerializerResolverFactory {

    @Override
    public Collection<TxSerializerResolver> txSerializerResolvers() {
        List<TxSerializerResolver> txSerializerResolvers = new ArrayList<>();
        txSerializerResolvers.add(DirectlyMatchResolver.INSTANCE);
        return txSerializerResolvers;
    }

}
