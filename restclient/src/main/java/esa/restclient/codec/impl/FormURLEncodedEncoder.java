package esa.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.MultipartBody;
import esa.restclient.MediaType;
import esa.restclient.RequestBodyContent;
import esa.restclient.codec.Encoder;

public class FormURLEncodedEncoder implements Encoder {

    @Override
    public RequestBodyContent<MultipartBody> encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }
        
        MultipartBody formBody = (MultipartBody) entity;
        formBody.multipartEncode(false);
        return RequestBodyContent.of(formBody);
    }
}
