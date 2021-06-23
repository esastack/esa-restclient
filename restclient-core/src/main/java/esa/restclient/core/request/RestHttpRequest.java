package esa.restclient.core.request;

import java.io.InputStream;

public interface RestHttpRequest extends HttpRequest{

    int maxRetries();

    int maxRedirects();

    /**
     * The readTimeout of current request
     *
     * @return readTimeout
     */
    long readTimeout();

    /**
     * Whether allow uri encode or not
     *
     * @return true or false
     */
    boolean uriEncode();

    boolean isUseExpectContinue();

    InputStream getBodyStream();

}
