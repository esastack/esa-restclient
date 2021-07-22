package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.BodyContent;
import esa.restclient.MediaType;

import java.io.File;

public class FileToFileEncoder implements Encoder {

    @Override
    public BodyContent<File> encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof File) {
            return BodyContent.of((File) entity);
        }

        throw new UnsupportedOperationException("FileToFileEncoder " +
                "only support encode file to file!entityClass:" + entity.getClass());
    }
}
