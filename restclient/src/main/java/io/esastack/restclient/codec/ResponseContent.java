package io.esastack.restclient.codec;

import io.esastack.restclient.codec.impl.ResponseContentImpl;

public interface ResponseContent extends Content {

    static ResponseContentImpl of(byte[] content) {
        return new ResponseContentImpl(content);
    }

}
