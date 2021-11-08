package io.esastack.restclient.codec;

import io.esastack.httpclient.core.MultipartBody;
import io.esastack.restclient.codec.impl.RequestContentImpl;

import java.io.File;

public interface RequestContent extends Content {

    static RequestContentImpl of(byte[] content) {
        return new RequestContentImpl(content);
    }

    static RequestContentImpl of(File content) {
        return new RequestContentImpl(content);
    }

    static RequestContentImpl of(MultipartBody content) {
        return new RequestContentImpl(content);
    }
}
