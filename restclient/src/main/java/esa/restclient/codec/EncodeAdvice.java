package esa.restclient.codec;

import esa.httpclient.core.util.Ordered;
import esa.restclient.RequestBodyContent;
import esa.restclient.RestRequest;

public interface EncodeAdvice extends Ordered {
    Object beforeEncode(RestRequest request, Object entity);

    void afterEncode(RestRequest request, Object entity, RequestBodyContent<?> content);
}
