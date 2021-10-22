package io.esastack.restclient;

import esa.commons.http.HttpMethod;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class RestClientTest {

    @Test
    void testMethod() {
        RestClient client = RestClient.ofDefault();
        String url = "http://localhost:8080/test";
        RestRequest request = client.get(url);
        then(request.method()).isEqualTo(HttpMethod.GET);

        request = client.post(url);
        then(request.method()).isEqualTo(HttpMethod.POST);

        request = client.head(url);
        then(request.method()).isEqualTo(HttpMethod.HEAD);

        request = client.delete(url);
        then(request.method()).isEqualTo(HttpMethod.DELETE);

        request = client.options(url);
        then(request.method()).isEqualTo(HttpMethod.OPTIONS);

        request = client.put(url);
        then(request.method()).isEqualTo(HttpMethod.PUT);
    }

}
