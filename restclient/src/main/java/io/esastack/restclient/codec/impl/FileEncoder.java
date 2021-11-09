package io.esastack.restclient.codec.impl;

import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestContent;

import java.io.File;

public class FileEncoder implements Encoder {

    @Override
    public RequestContent encode(EncodeContext encodeChainContext) throws Exception {

        if (File.class.isAssignableFrom(encodeChainContext.type())) {
            return RequestContent.of((File) encodeChainContext.entity());
        }
        return encodeChainContext.continueToEncode();
    }
}
