package io.esastack.restclient.codec;

import io.esastack.httpclient.core.MultipartBody;

import java.io.File;

public final class RequestBody<T> extends Body<T> {

    public static final class TYPE {
        public static final byte BYTES = (byte) 1;
        public static final byte FILE = (byte) 2;
        public static final byte MULTIPART = (byte) 3;
    }

    private RequestBody(byte type, T content) {
        super(type, content);
    }

    public static RequestBody<byte[]> of(byte[] result) {
        return new RequestBody<>(TYPE.BYTES, result);
    }

    public static RequestBody<File> of(File result) {
        return new RequestBody<>(TYPE.FILE, result);
    }

    public static RequestBody<MultipartBody> of(MultipartBody result) {
        return new RequestBody<>(TYPE.MULTIPART, result);
    }

    public boolean isBytes() {
        return getType() == TYPE.BYTES;
    }

    public byte[] getBytes() {
        return (byte[]) getContent();
    }

    public boolean isFile() {
        return getType() == TYPE.FILE;
    }

    public File getFile() {
        return (File) getContent();
    }

    public boolean isMultipart() {
        return getType() == TYPE.MULTIPART;
    }

    public MultipartBody getMultipart() {
        return (MultipartBody) getContent();
    }

}
