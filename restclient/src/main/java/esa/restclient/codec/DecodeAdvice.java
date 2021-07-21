package esa.restclient.codec;

import esa.restclient.RestRequest;
import esa.restclient.RestResponse;

import java.lang.reflect.Type;

public interface DecodeAdvice {
    void beforeDecode(RestRequest request, RestResponse response, Object data, Type type);

    Object afterDecode(RestRequest request, RestResponse response, Object data, Type type, Object decoded);
}
