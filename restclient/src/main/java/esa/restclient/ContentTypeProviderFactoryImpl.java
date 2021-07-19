package esa.restclient;

import esa.commons.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContentTypeProviderFactoryImpl implements ContentTypeProviderFactory {
    private static final ContentTypeProvider defaultContentTypeProvider
            = new DefaultContentTypeProvider();

    @Override
    public Collection<ContentTypeProvider> contentTypeProviders() {
        List<ContentTypeProvider> providers = new ArrayList<>();
        providers.add(defaultContentTypeProvider);
        return providers;
    }

    //TODO 丰富功能
    private static final class DefaultContentTypeProvider implements ContentTypeProvider {
        @Override
        public ContentType offer(HttpHeaders requestHeaders, Object entity) {
            if (entity == null) {
                return null;
            }

            Class<?> entityClass = entity.getClass();

            if (String.class.equals(entityClass)) {
                return ContentType.TEXT_PLAIN;
            } else {
                return ContentType.APPLICATION_JSON;
            }
        }
    }
}
