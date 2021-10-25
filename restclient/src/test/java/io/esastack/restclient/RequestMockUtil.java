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
import io.esastack.httpclient.core.CompositeRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.MultipartBody;
import io.esastack.restclient.exec.RestRequestExecutor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestMockUtil {

    private RequestMockUtil() {

    }

    public static RestCompositeRequest mockRequest(
            RestClientOptions clientOptions,
            RestRequestExecutor requestExecutor,
            Object requestContent,
            String responseContent, String responseCookieName, String responseCookieValue) {
        CompositeRequest request = mock(CompositeRequest.class);
        when(request.headers()).thenReturn(new Http1HeadersImpl());
        RestCompositeRequest restRequest = new RestCompositeRequest(request, clientOptions, requestExecutor);
        if (requestContent instanceof byte[]) {
            restRequest.entity((byte[]) requestContent);
        } else if (requestContent instanceof File) {
            restRequest.entity((File) requestContent);
        } else if (requestContent instanceof MultipartBody) {
            restRequest.multipart();
        } else {
            restRequest.entity(requestContent);
        }
        when(request.execute())
                .thenReturn(CompletableFuture.completedFuture(
                        createResponse(responseContent, responseCookieName, responseCookieValue)));
        return restRequest;
    }

    private static HttpResponse createResponse(String content, String cookieName, String cookieValue) {
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
