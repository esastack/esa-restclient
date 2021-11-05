package io.esastack.restclient;

import java.lang.reflect.Type;

public interface RestResponseBase extends RestResponse {
    <T> T bodyToEntity(Class<T> entityClass) throws Exception;

    <T> T bodyToEntity(Type genericType) throws Exception;
}
