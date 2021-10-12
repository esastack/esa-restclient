package esa.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.MultipartBody;
import esa.restclient.MediaType;
import esa.restclient.RequestBodyContent;
import esa.restclient.codec.Encoder;

public class MultipartToMultipartEncoder implements Encoder {

    @Override
    public RequestBodyContent<MultipartBody> encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof MultipartBody) {
            return RequestBodyContent.of((MultipartBody) entity);
        }

        throw new UnsupportedOperationException("MultipartToMultipartEncoder " +
                "only support encode multipart to multipart!entityClass:" + entity.getClass());
    }
}
