package io.esastack.restclient.codec;

public final class ResponseBody<T> extends Body<T>{

    public static final class TYPE {
        public static final byte BYTES = (byte) 1;
    }

    private ResponseBody(byte type, T content) {
        super(type, content);
    }

    public static ResponseBody<byte[]> of(byte[] result) {
        return new ResponseBody<>(RequestBody.TYPE.BYTES, result);
    }

    public boolean isBytes(){
        return RequestBody.TYPE.BYTES == getType();
    }

    public byte[] getBytes(){
        return (byte[]) getContent();
    }

}
