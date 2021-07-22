package esa.restclient;

import esa.httpclient.core.HttpRequestFacade;
import esa.restclient.codec.Multipart;

import java.io.File;

public final class BodyContent<T> {

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

    private BodyContent(int type, T content) {
        this.type = type;
        this.content = content;
    }

    public int type() {
        return type;
    }

    public T content() {
        return content;
    }

    void fillRequest(HttpRequestFacade request) {

    }

    public static BodyContent<byte[]> of(byte[] content) {
        return new BodyContent<>(TYPE.BYTES, content);
    }

    public static BodyContent<File> of(File content) {
        return new BodyContent<>(TYPE.FILE, content);
    }

    public static BodyContent<Multipart> of(Multipart content) {
        return new BodyContent<>(TYPE.MULTIPART, content);
    }

}
