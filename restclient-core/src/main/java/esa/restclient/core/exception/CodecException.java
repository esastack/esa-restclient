package esa.restclient.core.exception;

public class CodecException extends RuntimeException {

    private static final long serialVersionUID = -7491330351921926666L;

    public CodecException(String msg) {
        super(msg);
    }

    public CodecException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
