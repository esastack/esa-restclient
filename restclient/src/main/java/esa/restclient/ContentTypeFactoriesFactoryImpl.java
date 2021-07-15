package esa.restclient;

import esa.commons.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContentTypeFactoriesFactoryImpl implements ContentTypeFactoriesFactory {
    private static final RequestContentTypeFactory simpleContentTypeFactory
            = new SimpleContentTypeFactory();

    @Override
    public Collection<RequestContentTypeFactory> contentTypeFactories() {
        List<RequestContentTypeFactory> factories = new ArrayList<>();
        factories.add(simpleContentTypeFactory);
        return factories;
    }

    //TODO 丰富功能
    private static final class SimpleContentTypeFactory implements RequestContentTypeFactory {
        @Override
        public ContentType create(HttpHeaders requestHeaders, Object entity) {
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
