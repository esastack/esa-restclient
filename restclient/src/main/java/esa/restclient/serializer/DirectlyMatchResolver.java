package esa.restclient.serializer;

import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

public class DirectlyMatchResolver implements RxSerializerResolver, TxSerializerResolver {
    public static final DirectlyMatchResolver INSTANCE = new DirectlyMatchResolver();

    public static final int ORDER = 0;

    private DirectlyMatchResolver() {
    }

    @Override
    public TxSerializer resolve(HttpHeaders requestHeaders, ContentType contentType, Object entity) {
        if (contentType == null) {
            return null;
        }

        return contentType.txSerializer();
    }

    @Override
    public RxSerializer resolve(RestRequest request, ContentType[] acceptTypes,
                                MediaType mediaType, HttpHeaders responseHeaders, Type type) {
        if (acceptTypes != null && acceptTypes.length > 0) {
            for (ContentType contentType : acceptTypes) {
                if (contentType.getMediaType().includes(mediaType)) {
                    return contentType.rxSerializer();
                }
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
