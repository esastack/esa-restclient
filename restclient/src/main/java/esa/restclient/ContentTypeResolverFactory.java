package esa.restclient;

import java.util.Collection;

public interface ContentTypeResolverFactory {

    ContentTypeResolverFactory DEFAULT = new ContentTypeResolverFactoryImpl();

    Collection<ResponseContentTypeResolver> contentTypeResolvers();
}
