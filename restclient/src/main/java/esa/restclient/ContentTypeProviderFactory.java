package esa.restclient;

import java.util.Collection;

public interface ContentTypeProviderFactory {
    ContentTypeProviderFactory DEFAULT = new ContentTypeProviderFactoryImpl();

    Collection<ContentTypeProvider> contentTypeProviders();
}
