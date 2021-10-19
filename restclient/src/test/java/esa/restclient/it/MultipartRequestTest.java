package esa.restclient.it;

import esa.restclient.RestClient;
import esa.restclient.RestResponseBase;
import org.junit.jupiter.api.Test;
import org.mockserver.model.MediaType;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.BDDAssertions.then;

public class MultipartRequestTest {

    @Test
    void testMultipartRequest() throws Exception {
        String path = "/multipart";
        String responseContentString = "hello";
        byte[] responseContent = responseContentString.getBytes(StandardCharsets.UTF_8);
        MockServerUtil.startMockServer(
                null, responseContent, MediaType.TEXT_PLAIN, path);
        RestResponseBase response = RestClient.ofDefault().post("http://localhost:" + MockServerUtil.PORT + path)
                .multipart()
                .attr("aaa", "bbb")
                .attr("ccc", "ddd")
                .execute()
                .toCompletableFuture().get();
        then(response.bodyToEntity(String.class)).isEqualTo(responseContentString);
    }
}
