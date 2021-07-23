package esa.restclient.codec;

import esa.restclient.MediaType;
import esa.restclient.ResponseBodyContent;
import esa.restclient.RestRequest;
import esa.restclient.RestResponse;

import java.lang.reflect.Type;

public interface DecodeContext {

    RestRequest request();

    RestResponse response();

    MediaType mediaType();

    /**
     * Update media type of HTTP entity.
     *
     * @param mediaType new type for HTTP entity
     */
    void mediaType(MediaType mediaType);

    ResponseBodyContent<?> content();

    void content(ResponseBodyContent<?> content);

    Type type();

    Object proceed() throws Exception;
}
