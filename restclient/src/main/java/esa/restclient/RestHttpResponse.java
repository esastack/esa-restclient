package esa.restclient;

import esa.httpclient.core.Response;

import java.lang.reflect.Type;

public interface RestHttpResponse extends Response {

    <T> T bodyToEntity(Class<T> entityClass);

    <T> T bodyToEntity(Type type);
}
