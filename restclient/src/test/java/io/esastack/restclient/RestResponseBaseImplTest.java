package io.esastack.restclient;

import esa.commons.netty.http.CookieImpl;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.restclient.codec.DecodeAdvice;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestResponseBaseImplTest {

    @Test
    void testStatus() {
        RestRequest request = mock(RestRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        RestClientOptions clientOptions = mock(RestClientOptions.class);
        RestResponse restResponse = new RestResponseBaseImpl(request, response, clientOptions);
        when(response.status()).thenReturn(200);
        then(restResponse.status()).isEqualTo(200);
        when(response.status()).thenReturn(300);
        then(restResponse.status()).isEqualTo(300);
        when(response.status()).thenReturn(100);
        then(restResponse.status()).isEqualTo(100);
        when(response.status()).thenReturn(-1);
        then(restResponse.status()).isEqualTo(-1);
        when(response.status()).thenReturn(0);
        then(restResponse.status()).isEqualTo(0);
    }

    @Test
    void testHeaders() {
        RestRequest request = mock(RestRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        RestClientOptions clientOptions = mock(RestClientOptions.class);
        RestResponse restResponse = new RestResponseBaseImpl(request, response, clientOptions);
        HttpHeaders headers = new Http1HeadersImpl();
        headers.add("aaa", "aaa");
        headers.add("bbb", "bbb");
        when(response.headers()).thenReturn(headers);
        then(restResponse.headers().size()).isEqualTo(2);
        then(restResponse.headers().get("aaa")).isEqualTo("aaa");
        then(restResponse.headers().get("bbb")).isEqualTo("bbb");
        headers.add("aaa", "aaa1");
        then(restResponse.headers().getAll("aaa").size()).isEqualTo(2);
        then(restResponse.headers().getAll("aaa").get(1)).isEqualTo("aaa1");
        when(response.headers()).thenReturn(null);
        then(restResponse.headers()).isEqualTo(null);
    }

    @Test
    void testTrailers() {
        RestRequest request = mock(RestRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        RestClientOptions clientOptions = mock(RestClientOptions.class);
        RestResponse restResponse = new RestResponseBaseImpl(request, response, clientOptions);
        HttpHeaders headers = new Http1HeadersImpl();
        headers.add("aaa", "aaa");
        headers.add("bbb", "bbb");
        when(response.trailers()).thenReturn(headers);
        then(restResponse.trailers().size()).isEqualTo(2);
        then(restResponse.trailers().get("aaa")).isEqualTo("aaa");
        then(restResponse.trailers().get("bbb")).isEqualTo("bbb");
        headers.add("aaa", "aaa1");
        then(restResponse.trailers().getAll("aaa").size()).isEqualTo(2);
        then(restResponse.trailers().getAll("aaa").get(1)).isEqualTo("aaa1");
        when(response.trailers()).thenReturn(null);
        then(restResponse.trailers()).isEqualTo(null);
    }

    @Test
    void testVersion() {
        RestRequest request = mock(RestRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        RestClientOptions clientOptions = mock(RestClientOptions.class);
        RestResponse restResponse = new RestResponseBaseImpl(request, response, clientOptions);
        when(response.version()).thenReturn(HttpVersion.HTTP_1_0);
        then(restResponse.version()).isEqualTo(HttpVersion.HTTP_1_0);
        when(response.version()).thenReturn(HttpVersion.HTTP_1_1);
        then(restResponse.version()).isEqualTo(HttpVersion.HTTP_1_1);
        when(response.version()).thenReturn(null);
        then(restResponse.version()).isEqualTo(null);
    }

    @Test
    void testCookieOperate() {
        RestRequest request = mock(RestRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        RestClientOptions clientOptions = mock(RestClientOptions.class);
        RestResponseBase restResponse = new RestResponseBaseImpl(request, response, clientOptions);
        HttpHeaders headers = new Http1HeadersImpl();

        when(response.headers()).thenReturn(headers);
        then(restResponse.cookiesMap().size()).isEqualTo(0);
        then(restResponse.headers().getAll(HttpHeaderNames.SET_COOKIE).size()).isEqualTo(0);

        headers.add(HttpHeaderNames.SET_COOKIE, "aaa=aaa1");
        headers.add(HttpHeaderNames.SET_COOKIE, "aaa=aaa2");
        headers.add(HttpHeaderNames.SET_COOKIE, "bbb=bbb1");
        headers.add(HttpHeaderNames.SET_COOKIE, "bbb=bbb2");
        headers.add(HttpHeaderNames.SET_COOKIE, "ccc=ccc1");
        headers.add(HttpHeaderNames.SET_COOKIE, "ccc=ccc2");

        //test get cookies
        then(restResponse.cookies("aaa").size()).isEqualTo(2);
        then(restResponse.cookies("aaa").get(0).value()).isEqualTo("aaa1");
        then(restResponse.cookies("aaa").get(1).value()).isEqualTo("aaa2");
        then(restResponse.cookies("bbb").size()).isEqualTo(2);
        then(restResponse.cookies("bbb").get(0).value()).isEqualTo("bbb1");
        then(restResponse.cookies("bbb").get(1).value()).isEqualTo("bbb2");
        then(restResponse.cookies("ccc").size()).isEqualTo(2);
        then(restResponse.cookies("ccc").get(0).value()).isEqualTo("ccc1");
        then(restResponse.cookies("ccc").get(1).value()).isEqualTo("ccc2");
        then(restResponse.headers().getAll(HttpHeaderNames.SET_COOKIE).size()).isEqualTo(6);

        //test remove cookies
        List<Cookie> cookies = restResponse.removeCookies("aaa");
        then(cookies.size()).isEqualTo(2);
        then(cookies.get(0).value()).isEqualTo("aaa1");
        then(cookies.get(1).value()).isEqualTo("aaa2");
        then(restResponse.cookiesMap().size()).isEqualTo(2);
        then(restResponse.cookies("aaa").size()).isEqualTo(0);
        then(restResponse.cookiesMap().get("bbb").size()).isEqualTo(2);
        then(restResponse.cookiesMap().get("bbb").get(0).value()).isEqualTo("bbb1");
        then(restResponse.cookiesMap().get("bbb").get(1).value()).isEqualTo("bbb2");
        then(restResponse.cookiesMap().get("ccc").size()).isEqualTo(2);
        then(restResponse.cookiesMap().get("ccc").get(0).value()).isEqualTo("ccc1");
        then(restResponse.cookiesMap().get("ccc").get(1).value()).isEqualTo("ccc2");
        then(restResponse.headers().getAll(HttpHeaderNames.SET_COOKIE).size()).isEqualTo(4);

        //test remove cookies when the name of cookie is null
        cookies = restResponse.removeCookies(null);
        then(cookies.size()).isEqualTo(0);
        then(restResponse.cookiesMap().get("bbb").size()).isEqualTo(2);
        then(restResponse.cookiesMap().get("bbb").get(0).value()).isEqualTo("bbb1");
        then(restResponse.cookiesMap().get("bbb").get(1).value()).isEqualTo("bbb2");
        then(restResponse.cookiesMap().get("ccc").size()).isEqualTo(2);
        then(restResponse.cookiesMap().get("ccc").get(0).value()).isEqualTo("ccc1");
        then(restResponse.cookiesMap().get("ccc").get(1).value()).isEqualTo("ccc2");

        //test set cookie by cookie(cookie)
        assertDoesNotThrow(() ->
                restResponse.cookie(null)
        );
        restResponse.cookie(new CookieImpl("bbb", "bbb3"));
        then(restResponse.cookiesMap().get("bbb").size()).isEqualTo(3);
        then(restResponse.cookiesMap().get("bbb").get(2).value()).isEqualTo("bbb3");
        then(restResponse.headers().getAll(HttpHeaderNames.SET_COOKIE).size()).isEqualTo(5);

        //test set cookie by cookie(name, value)
        assertThrows(NullPointerException.class, () ->
                restResponse.cookie(null, "bbb4"));
        assertThrows(NullPointerException.class, () ->
                restResponse.cookie("bbb", null));
        restResponse.cookie("bbb", "bbb4");
        then(restResponse.cookiesMap().get("bbb").size()).isEqualTo(4);
        then(restResponse.cookiesMap().get("bbb").get(3).value()).isEqualTo("bbb4");
        then(restResponse.headers().getAll(HttpHeaderNames.SET_COOKIE).size()).isEqualTo(6);

        //test set cookies by cookies(cookies)
        assertDoesNotThrow(() ->
                restResponse.cookies((List<Cookie>) null)
        );
        List<Cookie> cookieList = new ArrayList<>();
        cookieList.add(new CookieImpl("bbb", "bbb5"));
        restResponse.cookies(cookieList);
        then(restResponse.cookiesMap().get("bbb").size()).isEqualTo(5);
        then(restResponse.cookiesMap().get("bbb").get(4).value()).isEqualTo("bbb5");
        then(restResponse.headers().getAll(HttpHeaderNames.SET_COOKIE).size()).isEqualTo(7);

        //test remove cookie by cookiesMap().remove(name)
        assertThrows(UnsupportedOperationException.class, () -> restResponse.cookiesMap().remove("aaa"));
    }

    @Test
    void testBodyToEntity() throws Exception {
        RestRequest request = mock(RestRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        RestClientOptions clientOptions = mock(RestClientOptions.class);
        RestResponseBase restResponse = new RestResponseBaseImpl(request, response, clientOptions);
        HttpHeaders headers = new Http1HeadersImpl();
        when(response.headers()).thenReturn(headers);
        when(response.body()).thenReturn(BufferUtil.buffer("Hello".getBytes(StandardCharsets.UTF_8)));
        //acceptTypes is null
        assertThrows(NullPointerException.class, () -> restResponse.bodyToEntity(String.class));

        //acceptTypes is empty
        when(request.acceptTypes()).thenReturn(new AcceptType[]{});
        assertThrows(IllegalStateException.class, () -> restResponse.bodyToEntity(String.class));

        //acceptTypes is not empty but contentType is null
        when(request.acceptTypes()).thenReturn(new AcceptType[]{AcceptType.TEXT_PLAIN});
        assertThrows(IllegalStateException.class, () -> restResponse.bodyToEntity(String.class));

        //acceptTypes is not match to contentType
        headers.add(HttpHeaderNames.CONTENT_TYPE, MediaTypeUtil.APPLICATION_JSON);
        when(request.acceptTypes()).thenReturn(new AcceptType[]{AcceptType.TEXT_PLAIN});
        assertThrows(IllegalStateException.class, () -> restResponse.bodyToEntity(String.class));

        //acceptTypes is match to contentType
        headers.set(HttpHeaderNames.CONTENT_TYPE, MediaTypeUtil.TEXT_PLAIN);
        when(request.acceptTypes()).thenReturn(new AcceptType[]{AcceptType.TEXT_PLAIN});
        then(restResponse.bodyToEntity(String.class)).isEqualTo("Hello");

        //acceptTypes is right and decodeAdvices is empty
        when(clientOptions.unmodifiableDecodeAdvices()).thenReturn(new DecodeAdvice[]{});
        then(restResponse.bodyToEntity(String.class)).isEqualTo("Hello");

        //acceptTypes is right and decodeAdvices is not null
        when(clientOptions.unmodifiableDecodeAdvices())
                .thenReturn(new DecodeAdvice[]{
                        context -> {
                            String result = (String) context.proceed();
                            return result + " Test1";
                        },
                        context -> {
                            String result = (String) context.proceed();
                            return result + " Test2";
                        }
                });
        then(restResponse.bodyToEntity(String.class)).isEqualTo("Hello Test2 Test1");
    }
}
