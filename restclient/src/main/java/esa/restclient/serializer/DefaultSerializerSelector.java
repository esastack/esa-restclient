package esa.restclient.serializer;

import esa.commons.annotation.Internal;
import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

@Internal
public class DefaultSerializerSelector implements RxSerializerSelector, TxSerializerSelector {

    public DefaultSerializerSelector() {
    }

    @Override
    public TxSerializer select(HttpHeaders requestHeaders, ContentType contentType, Object entity) {
        if (contentType == null) {
            return null;
        }

        return contentType.txSerializer();
    }

    @Override
    public RxSerializer select(RestRequest request, ContentType[] acceptTypes,
                               MediaType mediaType, HttpHeaders responseHeaders, Type type) {
        if (acceptTypes == null || acceptTypes.length == 0) {
            return null;
        }

        for (ContentType acceptType : acceptTypes) {
            if (acceptType.mediaType().isCompatibleWith(mediaType)) {
                return acceptType.rxSerializer();
            }
        }

        return null;
    }

    @Override
    public int getOrder() {
        return HIGHER_PRECEDENCE;
    }
}
