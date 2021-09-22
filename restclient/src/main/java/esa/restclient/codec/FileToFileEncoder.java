package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;
import esa.restclient.RequestBodyContent;

import java.io.File;

// TODO: FileToFile?
public class FileToFileEncoder implements Encoder {

    @Override
    public RequestBodyContent<File> encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof File) {
            return RequestBodyContent.of((File) entity);
        }

        throw new UnsupportedOperationException("FileToFileEncoder " +
                "only support encode file to file!entityClass:" + entity.getClass());
    }
}
