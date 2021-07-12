package esa.restclient;

import esa.commons.http.HttpHeaders;

import java.lang.reflect.Type;
import java.util.Optional;

public class DefaultContentTypeResolver implements ResponseContentTypeResolver {
    @Override
    public Optional<ContentType> resolve(RestRequest request, RequestContext context, HttpHeaders responseHeaders, Type type) {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }
}
