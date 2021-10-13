package esa.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;
import esa.restclient.RequestBodyContent;
import esa.restclient.codec.Encoder;

import java.io.File;

// TODO: FileToFile?
public class FileToFileEncoder implements Encoder {

    @Override
    public RequestBodyContent<File> encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }

        return RequestBodyContent.of((File) entity);
    }
}
