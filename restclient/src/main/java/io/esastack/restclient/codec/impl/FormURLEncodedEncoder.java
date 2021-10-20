package io.esastack.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.MultipartBody;
import io.esastack.restclient.RequestBodyContent;
import io.esastack.restclient.codec.Encoder;

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
