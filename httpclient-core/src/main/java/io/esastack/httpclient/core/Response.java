package io.esastack.httpclient.core;

import io.esastack.commons.net.http.HttpHeaders;

public interface Response extends HttpMessage {


    /**
     * Obtains {@link HttpHeaders} of trailing.
     *
     * @return headers
     */
    HttpHeaders trailers();
}
