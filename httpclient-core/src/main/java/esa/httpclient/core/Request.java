package esa.httpclient.core;

import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;

import java.util.List;
import java.util.Set;

public interface Request {
    HttpMethod method();

    String scheme();

    String path();

    HttpUri uri();

    Request addParam(String name, String value);

    String getParam(String name);

    /**
     * Obtains unmodifiable param values by specified {@code name}.
     *
     * @param name name
     * @return value
     */
    List<String> getParams(String name);

    /**
     * Obtains unmodifiable parameter names.
     *
     * @return names
     */
    Set<String> paramNames();

    HttpHeaders headers();

    Request addHeader(CharSequence name, CharSequence value);

    CharSequence getHeader(CharSequence name);

    Request setHeader(CharSequence name, CharSequence value);

    Request removeHeader(CharSequence name);

    /**
     * Whether allow uri encode or not
     *
     * @return true or false
     */
    boolean uriEncode();

    /**
     * The readTimeout of current request
     *
     * @return readTimeout
     */
    int readTimeout();
}
