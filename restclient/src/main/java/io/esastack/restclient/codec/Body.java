package io.esastack.restclient.codec;

public class Body {

    public enum Type {
        FILE, BYTES, MULTIPART
    }

    private Type type;
    private Object content;

    protected Body(Type type, Object content) {
        this.type = type;
        this.content = content;
    }

    public final Type type() {
        return type;
    }

    public final Object content() {
        return content;
    }
}
