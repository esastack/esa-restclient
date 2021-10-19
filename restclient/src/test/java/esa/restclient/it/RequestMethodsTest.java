package esa.restclient.it;

import esa.restclient.ContentType;
import esa.restclient.RequestBodyContent;
import esa.restclient.RestClient;
import esa.restclient.RestClientBuilder;
import esa.restclient.RestRequest;
import esa.restclient.RestResponse;
import io.esastack.commons.net.http.MediaTypeUtil;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Cookie;
import org.mockserver.model.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Java6BDDAssertions.then;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class RequestMethodsTest {

    private final int port = 13333;
    private final String path = "/hello";
    private final Person body = new Person("aaa", "bbb");
    private final static List<Cookie> EXPECTED_REQUEST_COOKIES = new ArrayList<>();
    private final static List<Cookie> EXPECTED_RESPONSE_COOKIES = new ArrayList<>();
    private RestClient restClient;

    static {
        EXPECTED_REQUEST_COOKIES.add(new Cookie("aaa", "aaa"));
        EXPECTED_REQUEST_COOKIES.add(new Cookie("bbb", "bbb"));
        EXPECTED_REQUEST_COOKIES.add(new Cookie("sessionId", "2By8LOhBmaW5nZXJwcmludCIlMDAzMW"));

        EXPECTED_RESPONSE_COOKIES.add(new Cookie("ccc", "ccc"));
        EXPECTED_RESPONSE_COOKIES.add(new Cookie("ddd", "ddd"));
        EXPECTED_RESPONSE_COOKIES.add(new Cookie("sessionId", "DDDDLOhBmaW5nZXJwcmludCIlMDAzMW"));

    }

    @Test
    void testPost() throws Exception {
        ClientAndServer mockServer = initMockServerWithBody("POST");
        RestClient restClient = buildRestClient();
        then(restClient.post("http://localhost:" + port + path).entity(body).execute()
                .toCompletableFuture().get().bodyToEntity(Person.class)).isEqualTo(body);
        mockServer.close();
    }

    @Test
    void testGet() throws Exception {
        ClientAndServer mockServer = initMockServerWithoutBody("GET");
        RestClient restClient = buildRestClient();
        then(restClient.get("http://localhost:" + port + path).execute()
                .toCompletableFuture().get().bodyToEntity(Person.class)).isEqualTo(body);
        mockServer.close();
    }

    @Test
    void testPut() throws Exception {
        ClientAndServer mockServer = initMockServerWithBody("PUT");
        RestClient restClient = buildRestClient();
        then(restClient.put("http://localhost:" + port + path).entity(body).execute()
                .toCompletableFuture().get().bodyToEntity(Person.class)).isEqualTo(body);
        mockServer.close();
    }

    @Test
    void testHead() throws Exception {
        ClientAndServer mockServer = initMockServerWithoutBody("HEAD");
        RestClient restClient = buildRestClient();
        restClient.head("http://localhost:" + port + path).execute()
                .toCompletableFuture().get();
        mockServer.close();
    }

    @Test
    void testOptions() throws Exception {
        ClientAndServer mockServer = initMockServerWithoutBody("OPTIONS");
        RestClient restClient = buildRestClient();
        then(restClient.options("http://localhost:" + port + path).execute()
                .toCompletableFuture().get().bodyToEntity(Person.class)).isEqualTo(body);
        mockServer.close();
    }

    @Test
    void testDelete() throws Exception {
        ClientAndServer mockServer = initMockServerWithBody("DELETE");
        RestClient restClient = buildRestClient();
        then(restClient.delete("http://localhost:" + port + path).entity(body).execute()
                .toCompletableFuture().get().bodyToEntity(Person.class)).isEqualTo(body);
        mockServer.close();
    }

    private ClientAndServer initMockServerWithBody(String method) throws Exception {
        ClientAndServer mockServer = startClientAndServer(port);
        mockServer.when(
                request().withMethod(method)
                        .withPath(path)
                        .withBody((byte[]) (
                                ContentType.APPLICATION_JSON_UTF8.encoder()
                                        .encode(MediaTypeUtil.APPLICATION_JSON_UTF8, null, body)
                                        .content()))
                        .withCookies(EXPECTED_REQUEST_COOKIES)
        ).respond(
                response().withStatusCode(200)
                        .withCookies(EXPECTED_RESPONSE_COOKIES)
                        .withContentType(MediaType.APPLICATION_JSON_UTF_8)
                        .withBody(
                                (byte[]) (ContentType.APPLICATION_JSON_UTF8.encoder()
                                        .encode(MediaTypeUtil.APPLICATION_JSON_UTF8, null, body)
                                        .content()))
        );
        return mockServer;
    }

    private ClientAndServer initMockServerWithoutBody(String method) throws Exception {
        ClientAndServer mockServer = startClientAndServer(port);
        mockServer.when(
                request().withMethod(method)
                        .withPath(path)
                        .withCookies(EXPECTED_REQUEST_COOKIES)
        ).respond(
                response().withStatusCode(200)
                        .withCookies(EXPECTED_RESPONSE_COOKIES)
                        .withContentType(MediaType.APPLICATION_JSON_UTF_8)
                        .withBody(
                                (byte[]) (ContentType.APPLICATION_JSON_UTF8.encoder()
                                        .encode(MediaTypeUtil.APPLICATION_JSON_UTF8, null, body)
                                        .content()))
        );
        return mockServer;
    }

    private RestClient buildRestClient() {
        if (restClient != null) {
            return restClient;
        }

        RestClientBuilder builder = RestClient.create();
        addCookieProcessInterceptor(builder);
        addAssertRequestBodyAdvice(builder);
        addAssertResponseBodyAdvice(builder);
        restClient = builder.build();
        return restClient;
    }

    private void addCookieProcessInterceptor(RestClientBuilder builder) {
        builder.addInterceptor((request, next) -> {
            addExpectCookies(request);
            return next.proceed(request).thenApply((response) -> {
                        assertExpectCookie(response);
                        return response;
                    }
            );
        });
    }

    private void addAssertRequestBodyAdvice(RestClientBuilder builder) {
        builder.addEncodeAdvice(encodeContext -> {
            RequestBodyContent<?> content = encodeContext.proceed();
            then(encodeContext.entity()).isEqualTo(body);
            return content;
        });
    }

    private void addAssertResponseBodyAdvice(RestClientBuilder builder) {
        builder.addDecodeAdvice(decodeContext -> {
            Person body = (Person) decodeContext.proceed();
            then(body).isEqualTo(body);
            return body;
        });
    }

    private void addExpectCookies(RestRequest request) {
        for (Cookie cookie : EXPECTED_REQUEST_COOKIES) {
            request.cookie(cookie.getName().toString(), cookie.getValue().toString());
        }
    }

    private void assertExpectCookie(RestResponse response) {
        for (Cookie expectCookie : EXPECTED_RESPONSE_COOKIES) {
            List<esa.commons.http.Cookie> cookies = response.getCookies(expectCookie.getName().toString());
            then(cookies.size()).isEqualTo(1);
            then(cookies.get(0).value()).isEqualTo(expectCookie.getValue().toString());
        }
    }

    private static class Person {
        private String name;
        private String value;

        public Person() {
        }

        public Person(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return Objects.equals(name, person.name) &&
                    Objects.equals(value, person.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

}
