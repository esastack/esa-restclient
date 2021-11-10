package io.esastack.restclient.codec.impl;

import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestContent;

import java.io.File;

public class FileEncoder implements Encoder<File> {

    @Override
    public RequestContent<File> encode(EncodeContext<File> encodeContext) throws Exception {

        if (File.class.isAssignableFrom(encodeContext.type())) {
            return RequestContent.of((File) encodeContext.entity());
        }
        return encodeContext.next();
    }
}
