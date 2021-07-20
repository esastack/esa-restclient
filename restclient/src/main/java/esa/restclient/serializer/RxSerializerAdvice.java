package esa.restclient.serializer;

import esa.httpclient.core.util.Ordered;
import esa.restclient.RestRequest;
import esa.restclient.RestResponse;

import java.lang.reflect.Type;

public interface RxSerializerAdvice extends Ordered {

    void beforeDeSerialize(RestRequest request, RestResponse response, Type type);

    Object afterDeSerialize(RestRequest request, RestResponse response, Type type, Object entity);
}
