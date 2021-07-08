package esa.restclient;

import esa.httpclient.core.Response;

import java.lang.reflect.Type;

public interface RestResponse extends Response {

    <T> T bodyToEntity(Class<T> entityClass);

    <T> T bodyToEntity(Type type);
}
