/*
 * Copyright 2021 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restclient.it;

import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restclient.RestClient;
import io.esastack.restclient.RestClientBuilder;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.codec.RequestContent;
import io.esastack.restclient.codec.impl.JacksonCodec;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Cookie;
import org.mockserver.model.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Java6BDDAssertions.then;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class RequestMethodsTest {

    private final String path = "/hello";
    private final Person body = new Person("aaa", "bbb");
    private static final List<Cookie> EXPECTED_REQUEST_COOKIES = new ArrayList<>();
    private static final List<Cookie> EXPECTED_RESPONSE_COOKIES = new ArrayList<>();
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
        initMockServerWithBody("POST");
        RestClient restClient = buildRestClient();
        then(restClient.post("http://localhost:" + MockServerUtil.PORT + path).entity(body).execute()
                .toCompletableFuture().get().bodyToEntity(Person.class)).isEqualTo(body);
    }

    @Test
    void testGet() throws Exception {
        initMockServerWithoutBody("GET");
        RestClient restClient = buildRestClient();
        then(restClient.get("http://localhost:" + MockServerUtil.PORT + path).execute()
                .toCompletableFuture().get().bodyToEntity(Person.class)).isEqualTo(body);
    }

    @Test
    void testPut() throws Exception {
        initMockServerWithBody("PUT");
        RestClient restClient = buildRestClient();
        then(restClient.put("http://localhost:" + MockServerUtil.PORT + path).entity(body).execute()
                .toCompletableFuture().get().bodyToEntity(Person.class)).isEqualTo(body);
    }

    @Test
    void testHead() throws Exception {
        initMockServerWithoutBody("HEAD");
        RestClient restClient = buildRestClient();
        restClient.head("http://localhost:" + MockServerUtil.PORT + path).execute()
                .toCompletableFuture().get();
    }

    @Test
    void testOptions() throws Exception {
        initMockServerWithoutBody("OPTIONS");
        RestClient restClient = buildRestClient();
        then(restClient.options("http://localhost:" + MockServerUtil.PORT + path).execute()
                .toCompletableFuture().get().bodyToEntity(Person.class)).isEqualTo(body);
    }

    @Test
    void testDelete() throws Exception {
        initMockServerWithBody("DELETE");
        RestClient restClient = buildRestClient();
        then(restClient.delete("http://localhost:" + MockServerUtil.PORT + path).entity(body).execute()
                .toCompletableFuture().get().bodyToEntity(Person.class)).isEqualTo(body);
    }

    private void initMockServerWithBody(String method) throws Exception {
        ClientAndServer mockServer = MockServerUtil.getMockServer();
        mockServer.when(
                request().withMethod(method)
                        .withPath(path)
                        .withBody(JacksonCodec.getDefaultMapper().writeValueAsBytes(body))
                        .withCookies(EXPECTED_REQUEST_COOKIES)
        ).respond(
                response().withStatusCode(HttpStatus.OK.code())
                        .withCookies(EXPECTED_RESPONSE_COOKIES)
                        .withContentType(MediaType.APPLICATION_JSON_UTF_8)
                        .withBody(JacksonCodec.getDefaultMapper().writeValueAsBytes(body))
        );
    }

    private void initMockServerWithoutBody(String method) throws Exception {
        ClientAndServer mockServer = MockServerUtil.getMockServer();
        mockServer.when(
                request().withMethod(method)
                        .withPath(path)
                        .withCookies(EXPECTED_REQUEST_COOKIES)
        ).respond(
                response().withStatusCode(HttpStatus.OK.code())
                        .withCookies(EXPECTED_RESPONSE_COOKIES)
                        .withContentType(MediaType.APPLICATION_JSON_UTF_8)
                        .withBody(JacksonCodec.getDefaultMapper().writeValueAsBytes(body))
        );
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
        builder.addEncodeAdvice(ctx -> {
            RequestContent<?> content = ctx.next();
            then(ctx.entity()).isEqualTo(body);
            return content;
        });
    }

    private void addAssertResponseBodyAdvice(RestClientBuilder builder) {
        builder.addDecodeAdvice(ctx -> {
            Person body = (Person) ctx.next();
            then(body).isEqualTo(body);
            return body;
        });
    }

    private void addExpectCookies(RestRequest request) {
        for (Cookie cookie : EXPECTED_REQUEST_COOKIES) {
            request.addCookie(cookie.getName().toString(), cookie.getValue().toString());
        }
    }

    private void assertExpectCookie(RestResponse response) {
        for (Cookie expectCookie : EXPECTED_RESPONSE_COOKIES) {
            then(response.cookie(expectCookie.getName().toString()).value())
                    .isEqualTo(expectCookie.getValue().toString());
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
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
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
