package io.esastack.restclient.codec;

public final class ResponseBody<T> extends Body<T> {

    private ResponseBody(Type type, T content) {
        super(type, content);
    }

    public static ResponseBody<byte[]> of(byte[] result) {
        return new ResponseBody<>(Type.BYTES, result);
    }

}
