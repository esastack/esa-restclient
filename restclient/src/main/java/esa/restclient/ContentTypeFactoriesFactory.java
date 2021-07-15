package esa.restclient;

import java.util.Collection;

public interface ContentTypeFactoriesFactory {
    ContentTypeFactoriesFactory DEFAULT = new ContentTypeFactoriesFactoryImpl();

    Collection<RequestContentTypeFactory> contentTypeFactories();
}
