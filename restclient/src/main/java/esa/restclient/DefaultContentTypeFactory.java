package esa.restclient;

import esa.commons.http.HttpHeaders;

import java.util.Optional;

public class DefaultContentTypeFactory implements RequestContentTypeFactory {
    //String 就返回 text，byte就返回流，对象就返回json jackson
    @Override
    public ContentType create(HttpHeaders requestHeaders, RequestContext context, Object entity) {
        if (entity == null) {
            return null;
        }

        Class entityClass = entity.getClass();


        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }
}
