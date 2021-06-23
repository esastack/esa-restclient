package esa.restclient.core.response;

import java.lang.reflect.Type;

public interface RestHttpResponse extends HttpResponse {

    <T> T bodyToEntity(Class<T> entityClass);

    <T> T bodyToEntity(Type type);
}
