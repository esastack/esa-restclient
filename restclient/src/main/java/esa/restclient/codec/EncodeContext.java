package esa.restclient.codec;

import esa.restclient.RequestBodyContent;
import esa.restclient.RestRequest;

public interface EncodeContext {

    RestRequest request();

    Object entity();

    void entity(Object entity);

    RequestBodyContent<?> proceed() throws Exception;
}
