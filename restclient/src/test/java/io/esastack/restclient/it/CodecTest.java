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

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.AcceptType;
import io.esastack.restclient.ContentType;
import io.esastack.restclient.RequestBodyContent;
import io.esastack.restclient.ResponseBodyContent;
import io.esastack.restclient.RestClient;
import io.esastack.restclient.RestResponseBase;
import io.esastack.restclient.codec.Decoder;
import org.junit.jupiter.api.Test;
import org.mockserver.model.MediaType;

import java.lang.reflect.Type;
import java.util.Objects;

import static org.assertj.core.api.BDDAssertions.then;

class CodecTest {

    private final RestClient restClient = RestClient.ofDefault();

    private final String path = "/codec";

    @Test
    void jsonCodecTest() throws Exception {
        Person requestEntity = new Person("LiMing", "aaa");
        Person responseEntity = new Person("WangHong", "bbb");
        MockServerUtil.startMockServer(
                (byte[]) (ContentType.APPLICATION_JSON_UTF8.encoder().encode(null, null, requestEntity).content()),
                (byte[]) (ContentType.APPLICATION_JSON_UTF8.encoder().encode(null, null, responseEntity).content()),
                MediaType.APPLICATION_JSON_UTF_8,
                path
        );

        RestResponseBase response = restClient.post("http://localhost:" + MockServerUtil.PORT + path)
                .contentType(ContentType.APPLICATION_JSON_UTF8)
                .accept(AcceptType.APPLICATION_JSON_UTF8)
                .entity(requestEntity)
                .execute()
                .toCompletableFuture()
                .get();
        then(response.bodyToEntity(Person.class)).isEqualTo(responseEntity);
    }

    @Test
    void stringCodecTest() throws Exception {
        String requestEntity = "requestEntity";
        String responseEntity = "responseEntity";
        MockServerUtil.startMockServer(
                (byte[]) (ContentType.TEXT_PLAIN.encoder().encode(null, null, requestEntity).content()),
                (byte[]) (ContentType.TEXT_PLAIN.encoder().encode(null, null, responseEntity).content()),
                MediaType.TEXT_PLAIN,
                path
        );

        RestResponseBase response = restClient.post("http://localhost:" + MockServerUtil.PORT + path)
                .contentType(ContentType.TEXT_PLAIN)
                .entity(requestEntity)
                .execute()
                .toCompletableFuture()
                .get();
        then(response.bodyToEntity(String.class)).isEqualTo(responseEntity);
    }

    @Test
    void customizeCodecTest() throws Exception {
        Person requestEntity = new Person("LiMing", "aaa");
        Person responseEntity = new Person("WangHong", "bbb");
        MockServerUtil.startMockServer(
                (byte[]) (ContentType.APPLICATION_JSON_UTF8.encoder().encode(null, null, requestEntity).content()),
                (byte[]) (ContentType.APPLICATION_JSON_UTF8.encoder().encode(null, null, responseEntity).content()),
                MediaType.APPLICATION_JSON_UTF_8,
                path
        );

        RestResponseBase response = restClient.post("http://localhost:" + MockServerUtil.PORT + path)
                .contentType(new ContentType(MediaTypeUtil.APPLICATION_JSON_UTF8,
                        (mediaType, headers, entity) ->
                                RequestBodyContent.of((byte[]) (ContentType.APPLICATION_JSON_UTF8.encoder()
                                        .encode(mediaType, headers, requestEntity).content()))))
                .accept(new AcceptType(MediaTypeUtil.APPLICATION_JSON_UTF8, new Decoder() {
                    @Override
                    public <T> T decode(io.esastack.commons.net.http.MediaType mediaType, HttpHeaders headers,
                                        ResponseBodyContent<?> content, Type type) throws Exception {
                        return AcceptType.APPLICATION_JSON_UTF8.decoder()
                                .decode(mediaType, headers, content, type);
                    }
                }))
                .entity(requestEntity)
                .execute()
                .toCompletableFuture()
                .get();
        then(response.bodyToEntity(Person.class)).isEqualTo(responseEntity);
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
