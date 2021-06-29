package esa.restclient;

import esa.commons.http.HttpMethod;
import esa.httpclient.core.Scheme;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultRestClientTest {

    private static RestClient restClient;
    private final String httpUrl = "http://localhost:8080";

    @BeforeAll
    static void initRestClient() {
        restClient = RestClient.ofDefault();
    }

    @Test
    void testGet() {
        HttpRequest request = restClient.get(httpUrl);
        assertEquals(request.method(), HttpMethod.GET);
        assertEquals(request.scheme().name(), Scheme.HTTP.name());
        assertEquals(request.uri().toString(), httpUrl);
        assertThrows(IllegalArgumentException.class, () -> restClient.get(null));
    }

    @Test
    void post() {
        HttpRequest request = restClient.post(httpUrl);
        assertEquals(request.method(), HttpMethod.POST);
        assertEquals(request.scheme().name(), Scheme.HTTP.name());
        assertEquals(request.uri().toString(), httpUrl);
        assertThrows(IllegalArgumentException.class, () -> restClient.post(null));
    }

    @Test
    void delete() {
        HttpRequest request = restClient.delete(httpUrl);
        assertEquals(request.method(), HttpMethod.DELETE);
        assertEquals(request.scheme().name(), Scheme.HTTP.name());
        assertEquals(request.uri().toString(), httpUrl);
        assertThrows(IllegalArgumentException.class, () -> restClient.delete(null));
    }

    @Test
    void put() {
        HttpRequest request = restClient.put(httpUrl);
        assertEquals(request.method(), HttpMethod.PUT);
        assertEquals(request.scheme().name(), Scheme.HTTP.name());
        assertEquals(request.uri().toString(), httpUrl);
        assertThrows(IllegalArgumentException.class, () -> restClient.put(null));
    }

    @Test
    void head() {
        HttpRequest request = restClient.head(httpUrl);
        assertEquals(request.method(), HttpMethod.HEAD);
        assertEquals(request.scheme().name(), Scheme.HTTP.name());
        assertEquals(request.uri().toString(), httpUrl);
        assertThrows(IllegalArgumentException.class, () -> restClient.head(null));
    }

    @Test
    void options() {
        HttpRequest request = restClient.options(httpUrl);
        assertEquals(request.method(), HttpMethod.OPTIONS);
        assertEquals(request.scheme().name(), Scheme.HTTP.name());
        assertEquals(request.uri().toString(), httpUrl);
        assertThrows(IllegalArgumentException.class, () -> restClient.options(null));
    }

    @Test
    void create() {
        RestClientBuilder builder = RestClient.create();
        RestClientBuilderTest.wrapWithBasicData(builder);
        RestClientBuilderTest.assertBasicData(builder, false);
    }

}
