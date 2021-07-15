package esa.restclient;

import esa.commons.http.HttpHeaders;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContentTypeResolversFactoryImpl implements ContentTypeResolversFactory {
    private static final ResponseContentTypeResolver simpleContentTypeResolver
            = new SimpleContentTypeResolver();

    @Override
    public Collection<ResponseContentTypeResolver> contentTypeResolvers() {
        List<ResponseContentTypeResolver> resolvers = new ArrayList<>();
        resolvers.add(simpleContentTypeResolver);
        return resolvers;
    }

    //TODO 丰富功能
    private static final class SimpleContentTypeResolver implements ResponseContentTypeResolver {
        @Override
        public ContentType resolve(RestRequest request, MediaType mediaType, HttpHeaders responseHeaders, Type type) {
            if (String.class.equals(type)) {
                return ContentType.TEXT_PLAIN;
            } else {
                return ContentType.APPLICATION_JSON;
            }
        }
    }
}
