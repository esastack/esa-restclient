package esa.restclient;

import esa.commons.http.HttpHeaders;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContentTypeResolverFactoryImpl implements ContentTypeResolverFactory {
    private static final ContentTypeResolver defaultContentTypeResolver
            = new DefaultContentTypeResolver();

    @Override
    public Collection<ContentTypeResolver> contentTypeResolvers() {
        List<ContentTypeResolver> resolvers = new ArrayList<>();
        resolvers.add(defaultContentTypeResolver);
        return resolvers;
    }

    //TODO 丰富功能
    private static final class DefaultContentTypeResolver implements ContentTypeResolver {
        @Override
        public ContentType resolve(RestRequest request, MediaType mediaType, HttpHeaders responseHeaders, Type type) {
            //TODO 如果获取Type 解析出来的 ContentType 与 实际Response的ContentType不一致怎么办，因此这里不应该用EntityClass来判断
            if (String.class.equals(type)) {
                return ContentType.TEXT_PLAIN;
            } else {
                return ContentType.APPLICATION_JSON;
            }
        }
    }
}
