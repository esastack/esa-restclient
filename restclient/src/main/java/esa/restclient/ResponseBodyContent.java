package esa.restclient;

public final class ResponseBodyContent<T> implements BodyContent<T> {
    /**
     * Use this flag to represent content type to avoid using Class.equal()
     * to improve performance
     */
    private final byte type;

    public static final class TYPE {
        public static final byte BYTES = 1;
    }

    private final T content;

    private ResponseBodyContent(byte type, T content) {
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

    public static ResponseBodyContent<byte[]> of(byte[] content) {
        return new ResponseBodyContent<>(ResponseBodyContent.TYPE.BYTES, content);
    }
}
