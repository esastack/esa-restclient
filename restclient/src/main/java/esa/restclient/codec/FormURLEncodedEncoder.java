package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.MultipartBody;
import esa.restclient.MediaType;
import esa.restclient.RequestBodyContent;

public class FormURLEncodedEncoder implements Encoder {

    @Override
    public RequestBodyContent<MultipartBody> encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof MultipartBody) {
            MultipartBody formBody = (MultipartBody) entity;
            formBody.multipartEncode(false);
            return RequestBodyContent.of(formBody);
        }

        throw new UnsupportedOperationException("FormEncoder " +
                "only support encode multipart to multipart!entityClass:" + entity.getClass());
    }
}
