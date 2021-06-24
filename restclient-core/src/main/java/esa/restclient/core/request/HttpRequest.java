package esa.restclient.core.request;

import esa.commons.http.Cookie;
import esa.commons.http.HttpMethod;
import esa.httpclient.core.HttpUri;
import esa.httpclient.core.Scheme;
import esa.restclient.core.HttpMessage;
import esa.restclient.core.MediaType;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface HttpRequest extends HttpMessage {

    Scheme scheme();

    HttpUri uri();

    HttpMethod method();

    String path();

    HttpRequest addParams(Map<String, String> params);

    HttpRequest addParam(String name, String value);

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

    HttpRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    HttpRequest addHeader(CharSequence name, CharSequence value);

    HttpRequest setHeader(CharSequence name, CharSequence value);

    String getHeader(CharSequence name);

    List<String> getHeaders(CharSequence name);

    List<String> removeHeaders(CharSequence name);

    MediaType contentType();

    HttpRequest contentType(MediaType mediaType);

    /**
     * Add a cookie to be set.
     *
     * @param cookie to be set.
     * @return the updated builder.
     */
    HttpRequest cookie(Cookie cookie);

    List<Cookie> removeCookies(String name);

    /**
     * Add a cookie to be set.
     *
     * @param name  the name of the cookie.
     * @param value the value of the cookie.
     * @return the updated builder.
     */
    HttpRequest cookie(String name, String value);

    List<Cookie> getCookies(String name);

    Map<String, List<Cookie>> getCookiesMap();

    /**
     * Add the accepted response media types.
     *
     * @param mediaTypes accepted response media types.
     * @return the updated builder.
     */
    HttpRequest accept(MediaType... mediaTypes);

    List<MediaType> acceptTypes();

    default InputStream getBodyStream() {
        return null;
    }
}
