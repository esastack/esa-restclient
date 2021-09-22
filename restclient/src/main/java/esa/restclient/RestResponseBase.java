package esa.restclient;

import java.lang.reflect.Type;

// TODO: RestResponseBase 和 RestResponse有什么区别？ 这两个方法放到RestResponse是否更合适？
public interface RestResponseBase extends RestResponse {
    <T> T bodyToEntity(Class<T> entityClass) throws Exception;

    <T> T bodyToEntity(Type type) throws Exception;
}
