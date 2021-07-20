package esa.restclient.serializer;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

public class ProtoBufRxSerializerSelector implements RxSerializerSelector {
    private final ProtoBufSerializer serializer;

    public ProtoBufRxSerializerSelector() {
        this.serializer = new ProtoBufSerializer();
    }

    public ProtoBufRxSerializerSelector(ProtoBufSerializer serializer) {
        Checks.checkNotNull(serializer, "serializer");
        this.serializer = serializer;
    }

    @Override
    public RxSerializer select(RestRequest request, ContentType[] acceptTypes,
                               MediaType mediaType, HttpHeaders responseHeaders, Type type) {
        if (MediaType.PROTOBUF.isCompatibleWith(mediaType)) {
            return serializer;
        }
        return null;
    }

    @Override
    public int getOrder() {
        return LOWER_PRECEDENCE;
    }
}
