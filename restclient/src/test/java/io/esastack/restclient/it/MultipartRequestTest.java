package io.esastack.restclient.it;

import io.esastack.restclient.RestClient;
import io.esastack.restclient.RestResponseBase;
import org.junit.jupiter.api.Test;
import org.mockserver.model.MediaType;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.BDDAssertions.then;

class MultipartRequestTest {

    @Test
    void testMultipartRequest() throws Exception {
        String path = "/multipart";
        String responseContentString = "hello";
        MockServerUtil.startMockServer(
                null,
                responseContentString.getBytes(StandardCharsets.UTF_8),
                MediaType.TEXT_PLAIN, path);
        RestResponseBase response = RestClient.ofDefault().post("http://localhost:" + MockServerUtil.PORT + path)
                .multipart()
                .attr("aaa", "bbb")
                .attr("ccc", "ddd")
                .execute()
                .toCompletableFuture().get();
        then(response.bodyToEntity(String.class)).isEqualTo(responseContentString);
    }
}
