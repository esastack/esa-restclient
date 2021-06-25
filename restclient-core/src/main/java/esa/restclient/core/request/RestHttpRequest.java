package esa.restclient.core.request;

import java.util.Map;
import java.util.Set;

public interface RestHttpRequest extends HttpRequest{

    int maxRetries();

    int maxRedirects();

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
