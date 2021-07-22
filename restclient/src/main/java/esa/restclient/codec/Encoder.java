package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.BodyContent;
import esa.restclient.MediaType;

public interface Encoder {

    BodyContent<?> encode(MediaType mediaType, HttpHeaders headers, Object entity) throws Exception;
}
