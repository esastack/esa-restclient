package esa.restclient.serializer;

import esa.httpclient.core.util.Ordered;
import esa.restclient.RestRequest;

public interface TxSerializerAdvice extends Ordered {

    Object beforeSerialize(RestRequest request, Object entity);

    void afterSerialize(RestRequest request, Object entity, byte[] data);
}
