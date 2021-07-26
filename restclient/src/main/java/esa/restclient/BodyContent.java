package esa.restclient;

public interface BodyContent<T> {
    byte type();

    T content();
}
