package io.esastack.restclient.codec;

import io.esastack.httpclient.core.MultipartBody;

import java.io.File;

public final class RequestBody extends Body {

    private RequestBody(Type type, Object content) {
        super(type, content);
    }

    public static RequestBody of(byte[] result) {
        return new RequestBody(Type.BYTES, result);
    }

    public static RequestBody of(File result) {
        return new RequestBody(Type.FILE, result);
    }

    public static RequestBody of(MultipartBody result) {
        return new RequestBody(Type.MULTIPART, result);
    }

    public boolean isFile() {
        return Type.FILE == type();
    }

    public boolean isBytes() {
        return Type.BYTES == type();
    }

    public boolean isMultipart() {
        return Type.MULTIPART == type();
    }
}
