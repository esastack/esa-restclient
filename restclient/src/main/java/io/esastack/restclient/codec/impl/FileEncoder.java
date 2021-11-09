package io.esastack.restclient.codec.impl;

import io.esastack.restclient.codec.EncodeChainContext;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestContent;

import java.io.File;

public class FileEncoder implements Encoder {

    @Override
    public RequestContent encode(EncodeChainContext encodeChainContext) throws Exception {

        if (File.class.isAssignableFrom(encodeChainContext.type())) {
            return RequestContent.of((File) encodeChainContext.entity());
        }
        return encodeChainContext.continueToEncode();
    }
}
