package esa.restclient.serializer;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

public class JsonRxSerializerSelector implements RxSerializerSelector {

    private final JsonSerializer serializer;

    public JsonRxSerializerSelector() {
        this.serializer = new JacksonSerializer();
    }

    public JsonRxSerializerSelector(JsonSerializer serializer) {
        Checks.checkNotNull(serializer, "Serializer must not be null");
        this.serializer = serializer;
    }

    @Override
    public RxSerializer select(RestRequest request, ContentType[] acceptTypes,
                               MediaType mediaType, HttpHeaders responseHeaders, Type type) {
        if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            return serializer;
        } else {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return LOWER_PRECEDENCE;
    }
}
