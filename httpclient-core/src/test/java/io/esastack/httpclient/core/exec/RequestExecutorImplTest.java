package io.esastack.httpclient.core.exec;

import io.esastack.httpclient.core.ExecContextUtil;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.mock.MockHttpResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RequestExecutorImplTest {

    @Test
    void testExecute() throws ExecutionException, InterruptedException {
        final String nameInInterceptor = "nameInInterceptor";
        final String valueInInterceptor = "valueInInterceptor";
        Interceptor[] interceptors = {(request, next) -> next.proceed(request).thenApply(response -> {
            response.headers().add(nameInInterceptor, valueInInterceptor);
            return response;
        })};
        final String nameInTransceiver = "nameInTransceiver";
        final String valueInTransceiver = "valueInTransceiver";
        final HttpTransceiver transceiver = (request, ctx) -> {
            HttpResponse response = new MockHttpResponse();
            response.headers().add(nameInTransceiver, valueInTransceiver);
            return CompletableFuture.completedFuture(response);
        };
        final RequestExecutorImpl executor = new RequestExecutorImpl(interceptors, transceiver);
        final ExecContext ctx = ExecContextUtil.newAs();
        final HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = executor.execute(request, ctx).get();
        assertEquals(valueInInterceptor, response.headers().get(nameInInterceptor));
        assertEquals(valueInTransceiver, response.headers().get(nameInTransceiver));
    }

}
