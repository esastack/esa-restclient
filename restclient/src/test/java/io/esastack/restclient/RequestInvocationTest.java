package io.esastack.restclient;

import esa.commons.http.Cookie;
import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.commons.netty.core.Buffer;
import esa.commons.netty.core.BufferImpl;
import esa.commons.netty.http.CookieImpl;
import esa.commons.netty.http.Http1HeadersImpl;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.httpclient.core.HttpResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestInvocationTest {

    @Test
    void testProceed() throws ExecutionException, InterruptedException {
        RestCompositeRequest request = mock(RestCompositeRequest.class);
        when(request.sendRequest())
                .thenReturn(CompletableFuture.completedFuture(
                        createResponse("Hi", "aaa", "aaa")));
        when(request.clientOptions())
                .thenReturn(mock(RestClientOptions.class));

        RequestInvocation requestInvocation = new RequestInvocation();
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

    private HttpResponse createResponse(String content, String cookieName, String cookieValue) {
        return new HttpResponse() {
            @Override
            public Buffer body() {
                Buffer buffer = new BufferImpl();
                buffer.writeBytes(content.getBytes(StandardCharsets.UTF_8));
                return buffer;
            }

            @Override
            public boolean aggregated() {
                return true;
            }

            @Override
            public HttpHeaders trailers() {
                return null;
            }

            @Override
            public int status() {
                return 200;
            }

            @Override
            public HttpVersion version() {
                return HttpVersion.HTTP_1_1;
            }

            @Override
            public HttpHeaders headers() {
                HttpHeaders headers = new Http1HeadersImpl();
                headers.add(HttpHeaderNames.CONTENT_TYPE, MediaTypeUtil.TEXT_PLAIN.toString());
                Cookie cookie = new CookieImpl(cookieName, cookieValue);
                headers.add(HttpHeaderNames.SET_COOKIE, cookie.encode(true));
                return headers;
            }
        };
    }

}
