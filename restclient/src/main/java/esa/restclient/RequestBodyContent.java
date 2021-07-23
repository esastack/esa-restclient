package esa.restclient;

import esa.restclient.codec.Multipart;

import java.io.File;

public final class RequestBodyContent<T> implements BodyContent<T> {

    /**
     * Use this flag to represent content type to avoid using Class.equal()
     * to improve performance
     */
    private final int type;

    public static final class TYPE {
        public static final int BYTES = 1;
        public static final int FILE = 2;
        public static final int MULTIPART = 3;
    }

    private final T content;

    private RequestBodyContent(int type, T content) {
        this.type = type;
        this.content = content;
    }

    @Override
    public int type() {
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
