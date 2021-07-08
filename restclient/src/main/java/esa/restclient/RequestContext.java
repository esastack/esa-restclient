package esa.restclient;

import java.util.Map;
import java.util.Set;

public interface RequestContext {
    /**
     * Return the request attribute value if present.
     *
     * @param name the attribute name
     * @return the attribute value
     */
    <T> T getProperty(String name);

    <T> T getProperty(String name, T defaultValue);

    void setProperty(String name, Object value);

    Set<String> propertyNames();

    Map<String, Object> properties();

    /**
     * Return the attributes of this request.
     */
    <T> T removeProperty(String name);
}
