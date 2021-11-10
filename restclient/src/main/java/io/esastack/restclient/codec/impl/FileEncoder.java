package io.esastack.restclient.codec.impl;

import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestContent;

import java.io.File;

public class FileEncoder implements Encoder {

    @Override
    public RequestContent<?> encode(EncodeContext<?> encodeContext) throws Exception {

        if (File.class.isAssignableFrom(encodeContext.type())) {
            return RequestContent.of((File) encodeContext.entity());
        }
        return encodeContext.next();
    }
}
