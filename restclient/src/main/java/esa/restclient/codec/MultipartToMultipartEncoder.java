package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;

public class MultipartToMultipartEncoder implements MultipartEncoder {

    @Override
    public Multipart encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof Multipart) {
            return (Multipart) entity;
        }

        throw new UnsupportedOperationException("FileToFileEncoder " +
                "only support encode multipart to multipart!entityClass:" + entity.getClass());
    }
}
