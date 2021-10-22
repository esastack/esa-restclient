package io.esastack.restclient;

import esa.commons.http.Cookie;
import esa.commons.http.HttpHeaderNames;
import esa.commons.netty.http.CookieImpl;
import io.esastack.httpclient.core.MultipartBody;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestCompositeRequestTest {

    @Test
    void testBasic() {
        RestRequestFacade request = RestClient.ofDefault()
                .post("http://localhost:8080/test")
                .readTimeout(9000)
                .maxRedirects(10)
                .maxRetries(10);
        then(request.readTimeout()).isEqualTo(9000);
        then(request.uri().toString()).isEqualTo("http://localhost:8080/test");
        then(request.path()).isEqualTo("/test");
        then(request.uriEncode()).isFalse();
        request.enableUriEncode();
        then(request.uriEncode()).isTrue();

        request.addParam("aaa", "aaa");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("bbb", "bbb");
        paramMap.put("ccc", "ccc");
        request.addParams(paramMap);
        then(request.paramNames().size()).isEqualTo(3);
        then(request.getParam("aaa")).isEqualTo("aaa");
        then(request.getParam("bbb")).isEqualTo("bbb");
        then(request.getParam("ccc")).isEqualTo("ccc");
        then(request.getParam("ddd")).isNull();

        request.addParam("aaa", "aaa1");
        then(request.getParams("aaa").size()).isEqualTo(2);
        List<String> paramValues = request.getParams("aaa");
        then(paramValues.get(0)).isEqualTo("aaa");
        then(paramValues.get(1)).isEqualTo("aaa1");
    }

    @Test
    void testHeaderOperate() {
        RestRequest request = RestClient.ofDefault().post("aaa");
        request.addHeader("aaa", "aaa");
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("bbb", "bbb");
        headerMap.put("ccc", "ccc");
        request.addHeaders(headerMap);

        then(request.headers().size()).isEqualTo(3);
        then(request.headers().get("aaa")).isEqualTo("aaa");
        then(request.headers().get("bbb")).isEqualTo("bbb");
        then(request.headers().get("ccc")).isEqualTo("ccc");
        then(request.headers().get("ddd")).isNull();

        request.removeHeader("aaa");
        request.setHeader("bbb","bbb2");
        then(request.headers().size()).isEqualTo(2);
        then(request.headers().get("aaa")).isNull();
        then(request.headers().get("bbb")).isEqualTo("bbb2");
        then(request.headers().get("ccc")).isEqualTo("ccc");
        then(request.headers().get("ddd")).isNull();

        assertThrows(NullPointerException.class, () ->
                request.removeHeader(null));
        assertDoesNotThrow(() ->
                request.addHeader(null, null));
        assertDoesNotThrow(() ->
                request.addHeaders(null));
    }

    @Test
    void testCookieOperate() {
        RestRequest request = RestClient.ofDefault().post("aaa");
        request.cookie("aaa", "aaa1");
        request.cookie("aaa", "aaa2");
        request.cookie(new CookieImpl("bbb", "bbb1"));
        request.cookie(new CookieImpl("bbb", "bbb2"));
        List<Cookie> cookieList = new ArrayList<>();
        cookieList.add(new CookieImpl("ccc", "ccc1"));
        cookieList.add(new CookieImpl("ccc", "ccc2"));
        request.cookies(cookieList);

        then(request.cookiesMap().size()).isEqualTo(3);
        then(request.cookiesMap().get("aaa").size()).isEqualTo(2);
        then(request.cookiesMap().get("aaa").get(0).value()).isEqualTo("aaa1");
        then(request.cookiesMap().get("aaa").get(1).value()).isEqualTo("aaa2");

        then(request.cookiesMap().get("bbb").size()).isEqualTo(2);
        then(request.cookiesMap().get("bbb").get(0).value()).isEqualTo("bbb1");
        then(request.cookiesMap().get("bbb").get(1).value()).isEqualTo("bbb2");

        then(request.cookiesMap().get("ccc").size()).isEqualTo(2);
        then(request.cookiesMap().get("ccc").get(0).value()).isEqualTo("ccc1");
        then(request.cookiesMap().get("ccc").get(1).value()).isEqualTo("ccc2");

        then(request.headers().getAll(HttpHeaderNames.COOKIE).size()).isEqualTo(6);

        List<Cookie> cookies = request.removeCookies("aaa");
        then(cookies.size()).isEqualTo(2);
        then(cookies.get(0).value()).isEqualTo("aaa1");
        then(cookies.get(1).value()).isEqualTo("aaa2");

        then(request.cookiesMap().size()).isEqualTo(2);
        then(request.cookiesMap().get("aaa")).isNull();

        then(request.cookiesMap().get("bbb").size()).isEqualTo(2);
        then(request.cookiesMap().get("bbb").get(0).value()).isEqualTo("bbb1");
        then(request.cookiesMap().get("bbb").get(1).value()).isEqualTo("bbb2");

        then(request.cookiesMap().get("ccc").size()).isEqualTo(2);
        then(request.cookiesMap().get("ccc").get(0).value()).isEqualTo("ccc1");
        then(request.cookiesMap().get("ccc").get(1).value()).isEqualTo("ccc2");

        then(request.headers().getAll(HttpHeaderNames.COOKIE).size()).isEqualTo(4);

        cookies = request.removeCookies(null);
        then(cookies.size()).isEqualTo(0);
        then(request.cookiesMap().get("bbb").size()).isEqualTo(2);
        then(request.cookiesMap().get("bbb").get(0).value()).isEqualTo("bbb1");
        then(request.cookiesMap().get("bbb").get(1).value()).isEqualTo("bbb2");

        then(request.cookiesMap().get("ccc").size()).isEqualTo(2);
        then(request.cookiesMap().get("ccc").get(0).value()).isEqualTo("ccc1");
        then(request.cookiesMap().get("ccc").get(1).value()).isEqualTo("ccc2");

        then(request.headers().getAll(HttpHeaderNames.COOKIE).size()).isEqualTo(4);

        assertThrows(UnsupportedOperationException.class, () -> request.cookiesMap().remove("aaa"));
        assertThrows(NullPointerException.class, () -> request.cookie("aaa", null));
        assertThrows(NullPointerException.class, () -> request.cookie(null, null));
        assertDoesNotThrow(() ->
                request.cookie(null)
        );
        assertDoesNotThrow(() ->
                request.cookies((List<Cookie>) null)
        );
    }

    @Test
    void testContentType() {
        RestRequestFacade request = RestClient.ofDefault().post("aaa");
        File file = new File("aaa");
        RestFileRequest restFileRequest = request.entity(file);
        then(restFileRequest.file()).isEqualTo(file);
        then(request.contentType()).isEqualTo(ContentType.FILE);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(ContentType.FILE.mediaType().toString());
        request.contentType(ContentType.APPLICATION_JSON_UTF8);
        then(request.contentType()).isEqualTo(ContentType.APPLICATION_JSON_UTF8);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(ContentType.APPLICATION_JSON_UTF8.mediaType().toString());

        RestRequestFacade finalRequest = request;
        assertThrows(IllegalStateException.class, () ->
                finalRequest.entity(new Object())
        );

        request = RestClient.ofDefault().post("aaa");
        Object entity = new Object();
        request.entity(entity);
        then(request.entity()).isEqualTo(entity);
        then(request.contentType()).isEqualTo(ContentType.APPLICATION_JSON_UTF8);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(ContentType.APPLICATION_JSON_UTF8.mediaType().toString());
        request.contentType(ContentType.APPLICATION_FORM_URLENCODED);
        then(request.contentType()).isEqualTo(ContentType.APPLICATION_FORM_URLENCODED);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(ContentType.APPLICATION_FORM_URLENCODED.mediaType().toString());
        RestRequestFacade finalRequest1 = request;
        assertThrows(IllegalStateException.class, () ->
                finalRequest1.entity(new Object())
        );

        request = RestClient.ofDefault().post("aaa");
        String aaa = "aaa";
        request.entity(aaa);
        then(request.entity()).isEqualTo(aaa);
        then(request.contentType()).isEqualTo(ContentType.TEXT_PLAIN);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(ContentType.TEXT_PLAIN.mediaType().toString());
        request.contentType(ContentType.APPLICATION_FORM_URLENCODED);
        then(request.contentType()).isEqualTo(ContentType.APPLICATION_FORM_URLENCODED);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(ContentType.APPLICATION_FORM_URLENCODED.mediaType().toString());
        RestRequestFacade finalRequest2 = request;
        assertThrows(IllegalStateException.class, () ->
                finalRequest2.entity(new Object())
        );

        request = RestClient.ofDefault().post("aaa");
        byte[] bytes = "aaa".getBytes();
        request.entity(bytes);
        then(request.entity()).isEqualTo(bytes);
        then(request.contentType()).isEqualTo(ContentType.APPLICATION_OCTET_STREAM);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(ContentType.APPLICATION_OCTET_STREAM.mediaType().toString());
        request.contentType(ContentType.APPLICATION_FORM_URLENCODED);
        then(request.contentType()).isEqualTo(ContentType.APPLICATION_FORM_URLENCODED);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(ContentType.APPLICATION_FORM_URLENCODED.mediaType().toString());
        RestRequestFacade finalRequest3 = request;
        assertThrows(IllegalStateException.class, () ->
                finalRequest3.entity(new Object())
        );

        assertThrows(NullPointerException.class, () -> finalRequest3.contentType(null));
    }

    @Test
    void testAcceptType() {
        RestRequestFacade request = RestClient.ofDefault().post("aaa");
        then(request.acceptTypes().length).isEqualTo(1);
        then(request.acceptTypes()[0]).isEqualTo(AcceptType.DEFAULT);

        assertThrows(NullPointerException.class, () -> request.accept(null));

        request.accept(AcceptType.APPLICATION_JSON_UTF8, AcceptType.APPLICATION_OCTET_STREAM);
        then(request.acceptTypes().length).isEqualTo(2);
        then(request.acceptTypes()[0]).isEqualTo(AcceptType.APPLICATION_JSON_UTF8);
        then(request.acceptTypes()[1]).isEqualTo(AcceptType.APPLICATION_OCTET_STREAM);

        then(request.getHeader(HttpHeaderNames.ACCEPT)).isNull();
    }

    @Test
    void testMultipart() {
        File file = new File("aaa");
        RestMultipartRequest request = RestClient.ofDefault().post("aaa").multipart()
                .file("aaa", file)
                .file("aaa1", file, "aaa1")
                .file("aaa2", file, "aaa2", false)
                .file("aaa3", "aaa", file, "aaa2", false)
                .file(null, null)
                .attr("bbb", "bbb")
                .attr(null, null);
        then(request.contentType()).isEqualTo(ContentType.MULTIPART_FORM_DATA);
        then(request.entity() instanceof MultipartBody).isTrue();

        MultipartBody body = (MultipartBody) request.entity();
        then(body.files().size()).isEqualTo(4);
        then(body.files().get(0).file()).isEqualTo(file);
        then(body.attrs().size()).isEqualTo(1);
        then(body.attrs().getFirst("bbb")).isEqualTo("bbb");
    }

}
