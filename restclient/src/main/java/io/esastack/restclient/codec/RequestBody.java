package io.esastack.restclient.codec;

import io.esastack.httpclient.core.MultipartBody;

import java.io.File;

public final class RequestBody<T> extends Body<T> {

    private RequestBody(Type type, T content) {
        super(type, content);
    }

    public static RequestBody<byte[]> of(byte[] result) {
        return new RequestBody<>(Type.BYTES, result);
    }

    public static RequestBody<File> of(File result) {
        return new RequestBody<>(Type.FILE, result);
    }

    public static RequestBody<MultipartBody> of(MultipartBody result) {
        return new RequestBody<>(Type.MULTIPART, result);
    }
}
