package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;

import java.io.File;

public class FileToFileEncoder implements FileEncoder {

    @Override
    public File encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof File) {
            return (File) entity;
        }

        throw new UnsupportedOperationException("FileToFileEncoder " +
                "only support encode file to file!entityClass:" + entity.getClass());
    }
}
