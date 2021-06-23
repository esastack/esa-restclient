package esa.restclient.core.request;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultExecutableRequestTest {

    static void testPropertyOperate(ExecutableRequest defaultHttpRequest) {
        assertEquals(0, defaultHttpRequest.propertyNames().size());
        String name = "name";
        String value = "value";
        String otherValue = "otherValue";
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.property(null, null));
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.property(null, value));
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.property(name, null));
        String valueGet = defaultHttpRequest.getProperty(name);
        assertNull(valueGet);
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.propertyNames().add("aaa"));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.properties().put("aaa", "aaa"));
        assertEquals(0, defaultHttpRequest.propertyNames().size());
        defaultHttpRequest.property(name, value);
        assertNotNull(defaultHttpRequest.getProperty(name));
        assertEquals(value, defaultHttpRequest.getProperty(name));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.propertyNames().add("aaa"));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.properties().put("aaa", "aaa"));
        assertEquals(1, defaultHttpRequest.propertyNames().size());
        assertEquals(1, defaultHttpRequest.properties().size());
        assertEquals(name, defaultHttpRequest.propertyNames().iterator().next());
        defaultHttpRequest.property(name, otherValue);

        assertNotNull(defaultHttpRequest.getProperty(name));
        assertEquals(otherValue, defaultHttpRequest.getProperty(name));
        assertEquals(1, defaultHttpRequest.propertyNames().size());
        assertEquals(1, defaultHttpRequest.properties().size());
        assertEquals(name, defaultHttpRequest.propertyNames().iterator().next());
        assertEquals(otherValue, defaultHttpRequest.removeProperty(name));
        assertEquals(0, defaultHttpRequest.propertyNames().size());
        assertEquals(0, defaultHttpRequest.properties().size());

        assertEquals("aaa", defaultHttpRequest.getProperty("aaa", "aaa"));
        assertEquals(0, defaultHttpRequest.propertyNames().size());
    }


}
