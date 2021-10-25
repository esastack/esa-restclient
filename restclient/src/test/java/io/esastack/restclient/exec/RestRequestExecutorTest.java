package io.esastack.restclient.exec;

import io.esastack.httpclient.core.MultipartBodyImpl;
import io.esastack.restclient.RequestMockUtil;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestCompositeRequest;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.codec.EncodeAdvice;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestRequestExecutorTest {

    @Test
    void testExecute() throws ExecutionException, InterruptedException {
        RestClientOptions clientOptions = mock(RestClientOptions.class);
        assertThrows(NullPointerException.class, () ->
                new RestRequestExecutorImpl(clientOptions));

        AtomicInteger passInterceptorNum = new AtomicInteger();
        when(clientOptions.unmodifiableInterceptors()).thenReturn(new ClientInterceptor[]{
                (request, next) -> {
                    passInterceptorNum.addAndGet(1);
                    return next.proceed(request);
                },
                (request, next) -> {
                    passInterceptorNum.addAndGet(1);
                    return next.proceed(request);
                }
        });

        AtomicInteger passEncodeAdviceNum = new AtomicInteger();
        when(clientOptions.unmodifiableEncodeAdvices()).thenReturn(new EncodeAdvice[]{
                context -> {
                    passEncodeAdviceNum.addAndGet(1);
                    return context.proceed();
                },
                context -> {
                    passEncodeAdviceNum.addAndGet(1);
                    return context.proceed();
                }
        });
        RestRequestExecutor requestExecutor = new RestRequestExecutorImpl(clientOptions);

        //entity is byte[]
        RestCompositeRequest request = RequestMockUtil.mockRequest(
                clientOptions, requestExecutor, "Hi".getBytes(),
                "Hi", "bbb", "bbb");
        RestResponse response = request.execute().toCompletableFuture().get();
        then(response.cookies("bbb").get(0).value()).isEqualTo("bbb");
        then(passInterceptorNum.get()).isEqualTo(2);
        then(passEncodeAdviceNum.get()).isEqualTo(2);

        //entity is file
        request = RequestMockUtil.mockRequest(
                clientOptions, requestExecutor, new File("Test"),
                "Hi", "bbb", "bbb");
        response = request.execute().toCompletableFuture().get();
        then(response.cookies("bbb").get(0).value()).isEqualTo("bbb");
        then(passInterceptorNum.get()).isEqualTo(4);
        then(passEncodeAdviceNum.get()).isEqualTo(4);

        //entity is multipartBody
        request = RequestMockUtil.mockRequest(
                clientOptions, requestExecutor, new MultipartBodyImpl(),
                "Hi", "bbb", "bbb");
        response = request.execute().toCompletableFuture().get();
        then(response.cookies("bbb").get(0).value()).isEqualTo("bbb");
        then(passInterceptorNum.get()).isEqualTo(6);
        then(passEncodeAdviceNum.get()).isEqualTo(6);
    }
}
