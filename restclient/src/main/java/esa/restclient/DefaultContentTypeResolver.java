package esa.restclient;

import esa.commons.http.HttpHeaders;

import java.lang.reflect.Type;
import java.util.Optional;

public class DefaultContentTypeResolver implements ResponseContentTypeResolver {
    @Override
    public Optional<ContentType> resolve(RestRequest request, HttpHeaders responseHeaders, Type type) {
        //TODO implement the method!
        return Optional.ofNullable(request.acceptTypes().get(0));
    }
}
