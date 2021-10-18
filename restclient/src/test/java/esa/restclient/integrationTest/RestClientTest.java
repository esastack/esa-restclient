package esa.restclient.integrationTest;

import esa.restclient.ContentType;
import esa.restclient.RequestBodyContent;
import esa.restclient.RestClient;
import esa.restclient.RestClientBuilder;
import esa.restclient.RestResponse;
import io.esastack.commons.net.http.MediaTypeUtil;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Cookie;
import org.mockserver.model.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class RestClientTest {

    private static final int PORT = 13333;
    private static final String PATH = "/hello";
    private static final Person BODY = new Person("aaa", "bbb");
    private static final List<Cookie> EXPECTED_REQUEST_COOKIES = new ArrayList<>();
    private static final List<Cookie> EXPECTED_RESPONSE_COOKIES = new ArrayList<>();

    static {
        EXPECTED_REQUEST_COOKIES.add(new Cookie("aaa", "aaa"));
        EXPECTED_REQUEST_COOKIES.add(new Cookie("bbb", "bbb"));
        EXPECTED_REQUEST_COOKIES.add(new Cookie("sessionId", "2By8LOhBmaW5nZXJwcmludCIlMDAzMW"));

        EXPECTED_RESPONSE_COOKIES.add(new Cookie("ccc", "ccc"));
        EXPECTED_RESPONSE_COOKIES.add(new Cookie("ddd", "ddd"));
        EXPECTED_RESPONSE_COOKIES.add(new Cookie("sessionId", "DDDDLOhBmaW5nZXJwcmludCIlMDAzMW"));

    }

    @Test
    public void testGet() throws Exception {
        ClientAndServer mockServer = startClientAndServer(PORT);
        mockServer.when(
                request().withMethod("GET")
                        .withPath(PATH)
                        .withCookies(EXPECTED_REQUEST_COOKIES)
        ).respond(
                response().withStatusCode(200)
                        .withCookies(EXPECTED_RESPONSE_COOKIES)
                        .withContentType(MediaType.APPLICATION_JSON_UTF_8)
                        .withBody(
                                (byte[]) (ContentType.APPLICATION_JSON_UTF8.encoder().encode(MediaTypeUtil.APPLICATION_JSON_UTF8, null, BODY).content()))
        );
        RestClient restClient = buildRestClient();
        System.out.println(restClient.get("http://localhost:" + PORT + PATH).execute()
                .toCompletableFuture().get().bodyToEntity(Person.class));
        mockServer.close();
    }

    private RestClient buildRestClient() {
        RestClientBuilder builder = RestClient.create();
        addCookieInterceptors(builder);
        addEncodeAdvices(builder);
        addDecodeAdvices(builder);
        return builder.build();
    }

    private static void addCookieInterceptors(RestClientBuilder builder) {
        builder.addInterceptor((request, next) -> {
            for (Cookie cookie : EXPECTED_REQUEST_COOKIES) {
                request.cookie(cookie.getName().toString(), cookie.getValue().toString());
            }
            return next.proceed(request).thenApply((response) -> {
                        assertCookie(response);
                        return response;
                    }
            );
        });
    }

    private static void assertCookie(RestResponse response) {
        for (Cookie expectCookie : EXPECTED_RESPONSE_COOKIES) {
            List<esa.commons.http.Cookie> cookies = response.getCookies(expectCookie.getName().toString());
            if (cookies.size() == 1) {
                if (!cookies.get(0).value().equals(expectCookie.getValue().toString())) {
                    throw new IllegalStateException("UnExpect cookies!cookies:" + cookies + "," +
                            "expectCookie:" + expectCookie);
                }
            } else {
                throw new IllegalStateException("UnExpect cookies!cookies:" + cookies + "," +
                        "expectCookie:" + expectCookie);
            }
        }
    }

    private static void addEncodeAdvices(RestClientBuilder builder) {
        builder.addEncodeAdvice(encodeContext -> {
            RequestBodyContent<?> content = encodeContext.proceed();
            if (content.content() != BODY) {
                throw new IllegalStateException("UnExpect body!body:" + content.content());
            }
            return content;
        });
    }

    private static void addDecodeAdvices(RestClientBuilder builder) {
        builder.addDecodeAdvice(decodeContext -> {
            Person body = (Person) decodeContext.proceed();
            if (!(BODY.name.equals(body.name) && BODY.value.equals(body.value))) {
                throw new IllegalStateException("UnExpect body!body:" + body);
            }
            return body;
        });
    }

    public static class Person {
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
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

}
