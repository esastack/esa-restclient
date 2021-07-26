package esa.restclient;

import esa.restclient.codec.Multipart;

import java.io.File;

public final class RequestBodyContent<T> implements BodyContent<T> {

    /**
     * Use this flag to represent content type to avoid using Class.equal()
     * to improve performance
     */
    private final byte type;

    public static final class TYPE {
        public static final byte BYTES = 1;
        public static final byte FILE = 2;
        public static final byte MULTIPART = 3;
    }

    private final T content;

    private RequestBodyContent(byte type, T content) {
        this.type = type;
        this.content = content;
    }

    @Override
    public byte type() {
        return type;
    }

    @Override
    public T content() {
        return content;
    }

    public static RequestBodyContent<byte[]> of(byte[] content) {
        return new RequestBodyContent<>(TYPE.BYTES, content);
    }

    public static RequestBodyContent<File> of(File content) {
        return new RequestBodyContent<>(TYPE.FILE, content);
    }

    public static RequestBodyContent<Multipart> of(Multipart content) {
        return new RequestBodyContent<>(TYPE.MULTIPART, content);
    }

}
