package io.esastack.restclient.codec;

import io.esastack.httpclient.core.MultipartBody;
import io.esastack.restclient.codec.impl.RequestContentImpl;

import java.io.File;

public interface RequestContent<V> extends Content<V> {

    static RequestContent<byte[]> of(byte[] content) {
        return new RequestContentImpl<>(content);
    }

    static RequestContent<File> of(File content) {
        return new RequestContentImpl<>(content);
    }

    static RequestContent<MultipartBody> of(MultipartBody content) {
        return new RequestContentImpl<>(content);
    }
}
