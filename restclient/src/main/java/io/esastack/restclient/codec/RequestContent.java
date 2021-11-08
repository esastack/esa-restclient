package io.esastack.restclient.codec;

import io.esastack.httpclient.core.MultipartBody;
import io.esastack.restclient.codec.impl.RequestContentImpl;

import java.io.File;

public interface RequestContent extends Content {

    static RequestContent of(byte[] content) {
        return new RequestContentImpl(content);
    }

    static RequestContent of(File content) {
        return new RequestContentImpl(content);
    }

    static RequestContent of(MultipartBody content) {
        return new RequestContentImpl(content);
    }
}
