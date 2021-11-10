package io.esastack.restclient.codec;

import io.esastack.commons.net.http.HttpHeaders;

public interface DecodeContext<V> extends DecodeChain {

    HttpHeaders headers();

    @Override
    ResponseContent<V> content();
}
