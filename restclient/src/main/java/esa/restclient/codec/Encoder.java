package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;
import esa.restclient.RequestBodyContent;

public interface Encoder {

    RequestBodyContent<?> encode(MediaType mediaType, HttpHeaders headers, Object entity) throws Exception;
}
