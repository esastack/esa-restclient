package esa.restclient.it;

import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.RequestBodyContent;
import esa.restclient.ResponseBodyContent;
import esa.restclient.RestClient;
import esa.restclient.RestResponseBase;
import esa.restclient.codec.Decoder;
import io.esastack.commons.net.http.MediaTypeUtil;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;

import java.lang.reflect.Type;
import java.util.Objects;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class CodecTest {

    private final RestClient restClient = RestClient.ofDefault();

    private final int PORT = 13333;
    private final String PATH = "/hello";

    @Test
    public void jsonCodecTest() throws Exception {
        Person requestEntity = new Person("LiMing", "aaa");
        Person responseEntity = new Person("WangHong", "bbb");
        ClientAndServer mockServer = startMockServer(
                (byte[]) (ContentType.APPLICATION_JSON_UTF8.encoder().encode(null, null, requestEntity).content()),
                (byte[]) (ContentType.APPLICATION_JSON_UTF8.encoder().encode(null, null, responseEntity).content()),
                MediaType.APPLICATION_JSON_UTF_8
        );

        RestResponseBase response = restClient.post("http://localhost:" + PORT + PATH)
                .contentType(ContentType.APPLICATION_JSON_UTF8)
                .accept(ContentType.APPLICATION_JSON_UTF8)
                .entity(requestEntity)
                .execute()
                .toCompletableFuture()
                .get();
        then(response.bodyToEntity(Person.class)).isEqualTo(responseEntity);

        mockServer.close();
    }

    @Test
    public void stringCodecTest() throws Exception {
        String requestEntity = "requestEntity";
        String responseEntity = "responseEntity";
        ClientAndServer mockServer = startMockServer(
                (byte[]) (ContentType.TEXT_PLAIN.encoder().encode(null, null, requestEntity).content()),
                (byte[]) (ContentType.TEXT_PLAIN.encoder().encode(null, null, responseEntity).content()),
                MediaType.TEXT_PLAIN
        );

        RestResponseBase response = restClient.post("http://localhost:" + PORT + PATH)
                .contentType(ContentType.TEXT_PLAIN)
                .accept(ContentType.TEXT_PLAIN)
                .entity(requestEntity)
                .execute()
                .toCompletableFuture()
                .get();
        then(response.bodyToEntity(String.class)).isEqualTo(responseEntity);

        mockServer.close();
    }

    @Test
    public void customizeCodecTest() throws Exception {
        Person requestEntity = new Person("LiMing", "aaa");
        Person responseEntity = new Person("WangHong", "bbb");
        ClientAndServer mockServer = startMockServer(
                (byte[]) (ContentType.APPLICATION_JSON_UTF8.encoder().encode(null, null, requestEntity).content()),
                (byte[]) (ContentType.APPLICATION_JSON_UTF8.encoder().encode(null, null, responseEntity).content()),
                MediaType.APPLICATION_JSON_UTF_8
        );

        RestResponseBase response = restClient.post("http://localhost:" + PORT + PATH)
                .contentType(ContentType.of(MediaTypeUtil.APPLICATION_JSON_UTF8,
                        (mediaType, headers, entity) ->
                                RequestBodyContent.of((byte[]) (ContentType.APPLICATION_JSON_UTF8.encoder()
                                        .encode(mediaType, headers, requestEntity).content()))))
                .accept(ContentType.of(MediaTypeUtil.APPLICATION_JSON_UTF8, new Decoder() {
                    @Override
                    public <T> T decode(io.esastack.commons.net.http.MediaType mediaType, HttpHeaders headers, ResponseBodyContent<?> content, Type type) throws Exception {
                        return ContentType.APPLICATION_JSON_UTF8.decoder()
                                .decode(mediaType, headers, content, type);
                    }
                }))
                .entity(requestEntity)
                .execute()
                .toCompletableFuture()
                .get();
        then(response.bodyToEntity(Person.class)).isEqualTo(responseEntity);

        mockServer.close();
    }

    private ClientAndServer startMockServer(byte[] requestBody, byte[] responseBody,
                                            MediaType responseMediaType) {
        ClientAndServer mockServer = startClientAndServer(PORT);
        mockServer.when(
                request().withMethod("POST")
                        .withPath(PATH)
                        .withBody(requestBody)
        ).respond(
                response().withStatusCode(200)
                        .withContentType(responseMediaType)
                        .withBody(responseBody)
        );
        return mockServer;
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
