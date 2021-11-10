package io.esastack.restclient.codec.impl;

import io.esastack.restclient.codec.RequestContent;

public final class RequestContentImpl<V> extends ContentImpl<V> implements RequestContent<V> {

    public RequestContentImpl(V value) {
        super(value);
    }

}
