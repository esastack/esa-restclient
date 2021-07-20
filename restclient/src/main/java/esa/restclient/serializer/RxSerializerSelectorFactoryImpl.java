package esa.restclient.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RxSerializerSelectorFactoryImpl implements RxSerializerSelectorFactory {

    @Override
    public Collection<RxSerializerSelector> rxSerializerSelectors() {
        List<RxSerializerSelector> rxSerializerSelectors = new ArrayList<>();
        rxSerializerSelectors.add(DefaultSerializerSelector.INSTANCE);
        rxSerializerSelectors.add(new JsonRxSerializerSelector(new JacksonSerializer()));
        rxSerializerSelectors.add(StringRxSerializerSelector.INSTANCE);
        rxSerializerSelectors.add(new ProtoBufRxSerializerSelector(new ProtoBufSerializer()));
        rxSerializerSelectors.add(ByteArrayRxSerializerSelector.INSTANCE);
        return rxSerializerSelectors;
    }

}
