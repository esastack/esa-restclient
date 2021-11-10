package io.esastack.restclient.codec.impl;

import io.esastack.restclient.codec.ResponseContent;

public class ResponseContentImpl<V> extends ContentImpl<V> implements ResponseContent<V> {

    public ResponseContentImpl(V value) {
        super(value);
    }

}
