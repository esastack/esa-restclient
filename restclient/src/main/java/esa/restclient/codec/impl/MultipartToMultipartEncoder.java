package esa.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.MultipartBody;
import esa.restclient.RequestBodyContent;
import esa.restclient.codec.Encoder;
import io.esastack.commons.net.http.MediaType;

public class MultipartToMultipartEncoder implements Encoder {

    @Override
    public RequestBodyContent<MultipartBody> encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }

        return RequestBodyContent.of((MultipartBody) entity);
    }
}
