package esa.restclient;

import java.lang.reflect.Type;

public interface RestResponseBase extends RestResponse {
    <T> T bodyToEntity(Class<T> entityClass) throws Exception;

    <T> T bodyToEntity(Type type) throws Exception;
}
