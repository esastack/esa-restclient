package esa.restclient.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RxSerializerResolverFactoryImpl implements RxSerializerResolverFactory {

    @Override
    public Collection<RxSerializerResolver> rxSerializerResolvers() {
        List<RxSerializerResolver> rxSerializerFactories = new ArrayList<>();
        rxSerializerFactories.add(DirectlyMatchResolver.INSTANCE);
        rxSerializerFactories.add(RxTypeMatchResolver.INSTANCE);
        return rxSerializerFactories;
    }

}
