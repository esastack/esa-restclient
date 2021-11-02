package io.esastack.restclient;

import io.esastack.restclient.codec.GenericType;

public interface RestResponseBase extends RestResponse {
    <T> T bodyToEntity(Class<T> entityClass) throws Exception;

    <T> T bodyToEntity(GenericType<T> genericType) throws Exception;
}
