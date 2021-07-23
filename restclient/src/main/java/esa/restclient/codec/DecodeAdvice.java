package esa.restclient.codec;

import esa.httpclient.core.util.Ordered;
import esa.restclient.ResponseBodyContent;
import esa.restclient.RestRequest;
import esa.restclient.RestResponse;

import java.lang.reflect.Type;

public interface DecodeAdvice extends Ordered {
    void beforeDecode(RestRequest request, RestResponse response, ResponseBodyContent<?> content, Type type);

    Object afterDecode(RestRequest request, RestResponse response, ResponseBodyContent<?> content, Object decoded, Type type);
}
