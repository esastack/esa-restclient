package io.esastack.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.RequestBodyContent;
import io.esastack.restclient.codec.Encoder;

import java.io.File;

public class FileToFileEncoder implements Encoder {

    @Override
    public RequestBodyContent<File> encode(MediaType mediaType, HttpHeaders headers, Object entity) {
        if (entity == null) {
            return RequestBodyContent.of((File) null);
        }

        return RequestBodyContent.of((File) entity);
    }
}
