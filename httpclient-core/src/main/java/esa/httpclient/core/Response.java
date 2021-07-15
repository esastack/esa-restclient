package esa.httpclient.core;

import esa.commons.http.HttpHeaders;
import esa.commons.netty.core.Buffer;

public interface Response extends HttpMessage {
    /**
     * Obtains body as {@link Buffer} format.
     *
     * @return body
     */
    Buffer body();

    /**
     * Obtains {@link HttpHeaders} of trailing.
     *
     * @return headers
     */
    HttpHeaders trailers();
}
