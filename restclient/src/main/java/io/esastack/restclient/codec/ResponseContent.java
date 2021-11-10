package io.esastack.restclient.codec;

import io.esastack.restclient.codec.impl.ResponseContentImpl;

public interface ResponseContent<V> extends Content<V> {

    static ResponseContent<byte[]> of(byte[] content) {
        return new ResponseContentImpl<>(content);
    }
}
