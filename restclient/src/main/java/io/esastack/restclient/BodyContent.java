package io.esastack.restclient;

public interface BodyContent<T> {



    byte type();

    T content();
}
