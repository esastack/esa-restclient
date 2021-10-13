package esa.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import esa.restclient.RequestBodyContent;
import esa.restclient.codec.Encoder;
import io.esastack.commons.net.http.MediaType;

import java.io.File;

public class FileToFileEncoder implements Encoder {

    @Override
    public RequestBodyContent<File> encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return null;
        }

        return RequestBodyContent.of((File) entity);
    }
}
