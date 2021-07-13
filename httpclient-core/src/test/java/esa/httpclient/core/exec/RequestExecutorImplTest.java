package esa.httpclient.core.exec;

import esa.httpclient.core.*;
import esa.httpclient.core.mock.MockHttpResponse;
import esa.httpclient.core.netty.HandleImpl;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class RequestExecutorImplTest {

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
        final HttpTransceiver transceiver = (request, ctx, handle, listener) -> {
            assertEquals(HandleImpl.class, handle.apply(null, null).getClass());
            HttpResponse response = new MockHttpResponse();
            response.headers().add(nameInTransceiver, valueInTransceiver);
            return CompletableFuture.completedFuture(response);
        };
        final RequestExecutorImpl executor = new RequestExecutorImpl(interceptors, transceiver);
        final Context ctx = mock(Context.class);
        final HttpRequest request = mock(HttpRequest.class);
        final Handler handler = mock(Handler.class);
        final Listener listener = mock(Listener.class);
        HttpResponse response = executor.execute(request, ctx, listener, null, handler).get();
        assertEquals(valueInInterceptor, response.headers().get(nameInInterceptor));
        assertEquals(valueInTransceiver, response.headers().get(nameInTransceiver));
    }

}
