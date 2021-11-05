package io.esastack.restclient.codec;

public final class ResponseBody extends Body {

    private ResponseBody(Type type, Object content) {
        super(type, content);
    }

    public static ResponseBody of(byte[] result) {
        return new ResponseBody(Type.BYTES, result);
    }

    public boolean isBytes() {
        return Type.BYTES == type();
    }
}
