package esa.restclient.codec;

import esa.restclient.RestRequest;

public interface EncodeAdvice {
    Object beforeEncode(RestRequest request, Object entity);

    void afterEncode(RestRequest request, Object encoded);
}
