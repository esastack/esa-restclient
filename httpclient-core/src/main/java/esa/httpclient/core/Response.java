package esa.httpclient.core;

import esa.commons.http.HttpHeaders;

public interface Response extends HttpMessage {


    /**
     * Obtains {@link HttpHeaders} of trailing.
     *
     * @return headers
     */
    HttpHeaders trailers();
}
