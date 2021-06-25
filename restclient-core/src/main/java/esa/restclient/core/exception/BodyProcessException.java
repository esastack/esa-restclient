package esa.restclient.core.exception;

public class BodyProcessException extends RuntimeException {

    private static final long serialVersionUID = -7491330351921926666L;

    public BodyProcessException(String msg) {
        super(msg);
    }

    public BodyProcessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
