package esa.restclient;

import java.util.Collection;

public interface ContentTypeResolversFactory {

    ContentTypeResolversFactory DEFAULT = new ContentTypeResolversFactoryImpl();

    Collection<ResponseContentTypeResolver> contentTypeResolvers();
}
