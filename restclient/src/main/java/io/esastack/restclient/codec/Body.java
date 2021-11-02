package io.esastack.restclient.codec;

public class Body<T> {

    private byte type;
    private T content;

    protected Body(byte type, T content) {
        this.type = type;
        this.content = content;
    }

    public byte getType() {
        return type;
    }

    public T getContent() {
        return content;
    }
}
