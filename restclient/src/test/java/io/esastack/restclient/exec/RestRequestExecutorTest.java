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
package io.esastack.restclient.exec;

import io.esastack.httpclient.core.MultipartBodyImpl;
import io.esastack.restclient.RequestMockUtil;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestCompositeRequest;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.codec.impl.ByteToByteCodec;
import io.esastack.restclient.codec.impl.FileEncoder;
import io.esastack.restclient.codec.impl.MultipartEncoder;
import io.esastack.restclient.codec.impl.StringCodec;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestRequestExecutorTest {

    @Test
    void testExecute() throws ExecutionException, InterruptedException {
        RestClientOptions clientOptions = mock(RestClientOptions.class);

        AtomicInteger passInterceptorNum = new AtomicInteger();
        when(clientOptions.unmodifiableInterceptors()).thenReturn(Arrays.asList(
                (request, next) -> {
                    passInterceptorNum.addAndGet(1);
                    return next.proceed(request);
                },
                (request, next) -> {
                    passInterceptorNum.addAndGet(1);
                    return next.proceed(request);
                })
        );

        AtomicInteger passEncodeAdviceNum = new AtomicInteger();
        when(clientOptions.unmodifiableEncodeAdvices()).thenReturn(Arrays.asList(
                context -> {
                    passEncodeAdviceNum.addAndGet(1);
                    return context.next();
                },
                context -> {
                    passEncodeAdviceNum.addAndGet(1);
                    return context.next();
                })
        );
        RestRequestExecutor requestExecutor = new RestRequestExecutorImpl(clientOptions);

        //entity is byte[]
        RestCompositeRequest request = RequestMockUtil.mockRequest(
                clientOptions,
                requestExecutor,
                new ByteToByteCodec(),
                new StringCodec(),
                "Hi".getBytes(),
                "Hi",
                "bbb",
                "bbb");
        RestResponse response = request.execute().toCompletableFuture().get();
        then(response.cookie("bbb").value()).isEqualTo("bbb");
        then(passInterceptorNum.get()).isEqualTo(2);
        then(passEncodeAdviceNum.get()).isEqualTo(2);

        //entity is file
        request = RequestMockUtil.mockRequest(
                clientOptions,
                requestExecutor,
                new FileEncoder(),
                new StringCodec(),
                new File("Test"),
                "Hi",
                "bbb",
                "bbb");
        response = request.execute().toCompletableFuture().get();
        then(response.cookie("bbb").value()).isEqualTo("bbb");
        then(passInterceptorNum.get()).isEqualTo(4);
        then(passEncodeAdviceNum.get()).isEqualTo(4);

        //entity is multipartBody
        request = RequestMockUtil.mockRequest(
                clientOptions,
                requestExecutor,
                new MultipartEncoder(),
                new StringCodec(),
                new MultipartBodyImpl(),
                "Hi",
                "bbb",
                "bbb");
        response = request.execute().toCompletableFuture().get();
        then(response.cookie("bbb").value()).isEqualTo("bbb");
        then(passInterceptorNum.get()).isEqualTo(6);
        then(passEncodeAdviceNum.get()).isEqualTo(6);
    }
}
