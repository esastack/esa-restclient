package io.esastack.restclient.codec;

public class Body<T> {

    public enum Type {
        FILE, BYTES, MULTIPART
    }

    private Type type;
    private T content;

    protected Body(Type type, T content) {
        this.type = type;
        this.content = content;
    }

    public final Type type() {
        return type;
    }

    public final T content() {
        return content;
    }
}
