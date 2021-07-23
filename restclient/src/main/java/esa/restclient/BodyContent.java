package esa.restclient;

public interface BodyContent<T> {
    int type();

    T content();
}
