package io.esastack.restclient.codec;

import io.esastack.commons.net.http.HttpHeaders;

public interface EncodeContext<V> extends EncodeChain {

    HttpHeaders headers();

    @Override
    RequestContent<V> next() throws Exception;
}
