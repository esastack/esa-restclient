package io.esastack.restclient;

import esa.commons.http.HttpHeaderNames;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.exec.RestRequestExecutor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class RequestInvocationTest {

    @Test
    void testProceed() throws ExecutionException, InterruptedException {
        RestCompositeRequest request = RequestMockUtil.mockRequest(
                mock(RestClientOptions.class), mock(RestRequestExecutor.class), "Hi".getBytes(),
                "Hi", "aaa", "aaa");

        RequestTransceiver requestInvocation = new RequestTransceiver();
        assertThrows(IllegalStateException.class,
                () -> requestInvocation.proceed(mock(RestRequest.class)));

        RestResponse response = requestInvocation.proceed(request).toCompletableFuture().get();
        then(response.status()).isEqualTo(200);
        then(response.headers().get(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(MediaTypeUtil.TEXT_PLAIN.toString());
        then(response.cookiesMap().size())
                .isEqualTo(1);
        then(response.cookies("aaa").get(0).value())
                .isEqualTo("aaa");
    }
}
