package esa.restclient;

import esa.commons.http.Cookie;

import java.util.Map;
import java.util.Set;

public interface RestHttpRequest extends HttpRequest {

    int maxRetries();

    int maxRedirects();

    @Override
    RestHttpRequest addParams(Map<String, String> params);

    @Override
    RestHttpRequest addParam(String name, String value);

    @Override
    RestHttpRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestHttpRequest addHeader(CharSequence name, CharSequence value);

    @Override
    RestHttpRequest setHeader(CharSequence name, CharSequence value);

    @Override
    RestHttpRequest cookie(Cookie cookie);

    @Override
    RestHttpRequest cookie(String name, String value);

    RestHttpRequest contentType(ContentType contentType);

    RestHttpRequest contentType(ContentTypeResolver contentTypeResolver);

    RestHttpRequest accept(AcceptType... acceptTypes);

    RestHttpRequest accept(AcceptTypeResolver acceptTypeResolver);

    /**
     * The readTimeout of current request
     *
     * @return readTimeout
     */
    int readTimeout();

    /**
     * Whether allow uri encode or not
     *
     * @return true or false
     */
    boolean uriEncode();

    boolean isUseExpectContinue();

    /**
     * Return the request attribute value if present.
     *
     * @param name the attribute name
     * @return the attribute value
     */
    <T> T getProperty(String name);

    <T> T getProperty(String name, T defaultValue);

    RestHttpRequest property(String name, Object value);

    Set<String> propertyNames();

    Map<String, Object> properties();

    /**
     * Return the attributes of this request.
     */
    <T> T removeProperty(String name);

}
