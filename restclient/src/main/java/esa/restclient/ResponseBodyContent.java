package esa.restclient;

public final class ResponseBodyContent<T> implements BodyContent<T> {
    /**
     * Use this flag to represent content type to avoid using Class.equal()
     * to improve performance
     */
    private final int type;

    public static final class TYPE {
        public static final int BYTES = 1;
    }

    private final T content;

    private ResponseBodyContent(int type, T content) {
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

    public static ResponseBodyContent<byte[]> of(byte[] content) {
        return new ResponseBodyContent<>(ResponseBodyContent.TYPE.BYTES, content);
    }
}
