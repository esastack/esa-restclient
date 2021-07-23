package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;
import esa.restclient.RequestBodyContent;

public class MultipartToMultipartEncoder implements Encoder {

    @Override
    public RequestBodyContent<Multipart> encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof Multipart) {
            return RequestBodyContent.of((Multipart) entity);
        }

        throw new UnsupportedOperationException("FileToFileEncoder " +
                "only support encode multipart to multipart!entityClass:" + entity.getClass());
    }
}
