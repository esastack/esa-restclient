package esa.restclient.request;

import esa.commons.http.Cookie;
import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpMethod;
import esa.commons.http.HttpVersion;
import esa.commons.netty.http.CookieImpl;
import esa.httpclient.core.Scheme;
import esa.restclient.MediaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Null;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultHttpRequestTest {

    private final static HttpMethod method = HttpMethod.GET;
    private final static String httpUrl = "http://localhost:8080";
    private final static String httpsUrl = "https://localhost:8080";
    private final static String path = "/test";
    private final static HttpVersion httpVersion = HttpVersion.HTTP_1_1;


    @Test
    void testCreate() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultHttpRequest(null, method, httpVersion));
        assertThrows(NullPointerException.class, () -> new DefaultHttpRequest(httpUrl, null, httpVersion));

        DefaultHttpRequest defaultHttpRequest = new DefaultHttpRequest(httpUrl, method, httpVersion);
        assertEquals(method, defaultHttpRequest.method());
        assertEquals(httpUrl, defaultHttpRequest.uri().toString());
        assertEquals(Scheme.HTTP, defaultHttpRequest.scheme());
        assertEquals("", defaultHttpRequest.path());

        defaultHttpRequest = new DefaultHttpRequest(httpsUrl + path, method, httpVersion);
        assertEquals(method, defaultHttpRequest.method());
        assertEquals(httpsUrl + path, defaultHttpRequest.uri().toString());
        assertEquals(Scheme.HTTPS, defaultHttpRequest.scheme());
        assertEquals(path, defaultHttpRequest.path());

        defaultHttpRequest = new DefaultHttpRequest(defaultHttpRequest);
        assertEquals(method, defaultHttpRequest.method());
        assertEquals(httpsUrl + path, defaultHttpRequest.uri().toString());
        assertEquals(Scheme.HTTPS, defaultHttpRequest.scheme());
        assertEquals(path, defaultHttpRequest.path());
    }

    @Test
    void testParamOperate() {
        testParamOperate(new DefaultHttpRequest(httpUrl, method, httpVersion));
    }

    @Test
    void testParamsOperate() {
        testParamsOperate(new DefaultHttpRequest(httpUrl, method, httpVersion));
    }

    @Test
    void testHeaderOperate() {
        testHeaderOperate(new DefaultHttpRequest(httpUrl, method, httpVersion));
    }

    @Test
    void testHeadersOperate() {
        testHeadersOperate(new DefaultHttpRequest(httpUrl, method, httpVersion));
    }

    @Test
    void testAcceptOperate() {
        testAcceptOperate(new DefaultHttpRequest(httpUrl, method, httpVersion));
    }

    @Test
    void testContentTypeOperate() {
        testContentTypeOperate(new DefaultHttpRequest(httpUrl, method, httpVersion));
    }


    @Test
    void testHttpVersionOperate() {
        assertThrows(NullPointerException.class, () -> new DefaultHttpRequest(httpUrl, method, null));
        testHttpVersion(new DefaultHttpRequest(httpUrl, method, HttpVersion.HTTP_1_0), HttpVersion.HTTP_1_0);
        testHttpVersion(new DefaultHttpRequest(httpUrl, method, HttpVersion.HTTP_1_1), HttpVersion.HTTP_1_1);
        testHttpVersion(new DefaultHttpRequest(httpUrl, method, HttpVersion.HTTP_2), HttpVersion.HTTP_2);
    }

    @Test
    void testCookieOperate() {
        testCookieOperate(new DefaultHttpRequest(httpUrl, method, httpVersion));
    }


    static void testParamOperate(HttpRequest defaultHttpRequest) {
        String name = "name";
        String value = "value";

        assertThrows(NullPointerException.class, () -> defaultHttpRequest.addParam(null, value));
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.addParam(name, null));
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.getParam(null));
        assertNull(defaultHttpRequest.getParam(name));
        assertEquals(0, defaultHttpRequest.paramNames().size());
        assertEquals(0, defaultHttpRequest.getParams(name).size());

        defaultHttpRequest.addParam(name, value);
        assertEquals(value, defaultHttpRequest.getParam(name));
        assertEquals(1, defaultHttpRequest.paramNames().size());

        assertTrue(defaultHttpRequest.paramNames().contains(name));
        assertEquals(1, defaultHttpRequest.getParams(name).size());
        assertTrue(defaultHttpRequest.getParams(name).contains(value));
    }

    static void testParamsOperate(HttpRequest defaultHttpRequest) {
        String noValueName = "noValueName";
        String name = "name";
        String value = "value";

        Map<String, String> params = new HashMap<>();
        defaultHttpRequest.addParams(null);
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.getParams(null));
        assertEquals(0, defaultHttpRequest.getParams(name).size());
        assertEquals(0, defaultHttpRequest.paramNames().size());
        assertNull(defaultHttpRequest.getParam(name));

        params.put(name, value);
        defaultHttpRequest.addParams(params);
        assertEquals(value, defaultHttpRequest.getParam(name));
        assertEquals(1, defaultHttpRequest.paramNames().size());
        assertTrue(defaultHttpRequest.paramNames().contains(name));
        assertEquals(1, defaultHttpRequest.getParams(name).size());
        assertTrue(defaultHttpRequest.getParams(name).contains(value));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.getParams(name).add("aaa"));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.paramNames().add("aaa"));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.getParams(noValueName).add("aaa"));
    }

    static void testHeaderOperate(HttpRequest defaultHttpRequest) {
        defaultHttpRequest.headers().clear();
        String name = "name";
        String value = "value";
        String otherValue = "otherValue";

        assertThrows(NullPointerException.class, () -> defaultHttpRequest.addHeader(null, value));
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.addHeader(value, null));
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.setHeader(null, value));
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.setHeader(value, null));
        assertNull(defaultHttpRequest.getHeader(name));
        Assertions.assertEquals(0, defaultHttpRequest.headers().size());

        defaultHttpRequest.addHeader(name, value);
        assertEquals(value, defaultHttpRequest.getHeader(name));
        Assertions.assertEquals(1, defaultHttpRequest.headers().size());
        Assertions.assertTrue(defaultHttpRequest.headers().contains(name));

        defaultHttpRequest.setHeader(name, otherValue);
        assertEquals(otherValue, defaultHttpRequest.getHeader(name));
        Assertions.assertEquals(1, defaultHttpRequest.headers().size());
        Assertions.assertTrue(defaultHttpRequest.headers().contains(name));


        defaultHttpRequest.removeHeaders(name);
        assertNull(defaultHttpRequest.getHeader(name));
        Assertions.assertEquals(0, defaultHttpRequest.headers().size());
        Assertions.assertFalse(defaultHttpRequest.headers().contains(name));
    }

    static void testHeadersOperate(HttpRequest defaultHttpRequest) {
        defaultHttpRequest.headers().clear();
        String noValueName = "noValueName";
        String name = "name";
        String value = "value";

        Map<String, String> headers = new HashMap<>();
        headers.put(name, value);
        assertNull(defaultHttpRequest.getHeader(name));
        Assertions.assertEquals(0, defaultHttpRequest.headers().size());

        defaultHttpRequest.addHeaders(null);
        assertNull(defaultHttpRequest.getHeader(name));
        defaultHttpRequest.addHeaders(headers);
        assertEquals(value, defaultHttpRequest.getHeader(name));
        Assertions.assertEquals(1, defaultHttpRequest.headers().size());
        Assertions.assertTrue(defaultHttpRequest.headers().contains(name));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.getHeaders(name).add("aaa"));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.removeHeaders(name).add("aaa"));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.getHeaders(noValueName).add("aaa"));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.removeHeaders(noValueName).add("aaa"));
    }

    static void testContentTypeOperate(HttpRequest defaultHttpRequest) {
        defaultHttpRequest.headers().clear();
        defaultHttpRequest.contentType(null);
        Assertions.assertEquals(0, defaultHttpRequest.headers().size());
        assertNull(defaultHttpRequest.contentType());

        defaultHttpRequest.contentType(MediaType.APPLICATION_JSON);
        MediaType mediaType = MediaType.parseMediaType(defaultHttpRequest.headers().get(HttpHeaderNames.CONTENT_TYPE));
        assertEquals(MediaType.APPLICATION_JSON, mediaType);
        Assertions.assertEquals(MediaType.APPLICATION_JSON, defaultHttpRequest.contentType());
        Assertions.assertEquals(1, defaultHttpRequest.headers().size());

        defaultHttpRequest.contentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        Assertions.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, defaultHttpRequest.contentType().toString());
        Assertions.assertEquals(StandardCharsets.UTF_8, defaultHttpRequest.contentType().charset());
        Assertions.assertEquals(1, defaultHttpRequest.headers().size());
    }

    static void testAcceptOperate(HttpRequest defaultHttpRequest) {
        defaultHttpRequest.headers().clear();
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.acceptTypes().add(MediaType.APPLICATION_FORM_URLENCODED));
        defaultHttpRequest.accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED);
        Assertions.assertEquals(2, defaultHttpRequest.headers().getAll(HttpHeaderNames.ACCEPT).size());
        List<MediaType> mediaTypes = defaultHttpRequest.acceptTypes();
        assertEquals(2, mediaTypes.size());
        assertTrue(mediaTypes.contains(MediaType.APPLICATION_JSON));
        assertTrue(mediaTypes.contains(MediaType.APPLICATION_FORM_URLENCODED));

        defaultHttpRequest.accept(MediaType.APPLICATION_JSON);
        defaultHttpRequest.accept(null);
        mediaTypes = defaultHttpRequest.acceptTypes();
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.acceptTypes().add(MediaType.APPLICATION_FORM_URLENCODED));
        assertEquals(1, mediaTypes.size());
        assertTrue(mediaTypes.contains(MediaType.APPLICATION_JSON));
    }

    static void testHttpVersion(HttpRequest defaultHttpRequest, HttpVersion httpVersion) {
        Assertions.assertEquals(defaultHttpRequest.version(), httpVersion);
    }


    static void testCookieOperate(HttpRequest defaultHttpRequest) {
        String noValueName = "noValueName";
        String name = "name";
        String otherName = "otherName";
        String value = "value";
        String otherValue = "otherValue";
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.cookie(null, null));
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.cookie(null, value));
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.cookie(name, null));
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.cookie(new CookieImpl(null)));
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.cookie(new CookieImpl(null, null)));
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.cookie(new CookieImpl(null, value)));
        assertThrows(NullPointerException.class, () -> defaultHttpRequest.cookie(new CookieImpl(name, null)));

        defaultHttpRequest.cookie(null);
        assertEquals(0, defaultHttpRequest.getCookiesMap().size());
        assertEquals(0, defaultHttpRequest.getCookies(name).size());
        defaultHttpRequest.cookie(name, value);
        assertEquals(1, defaultHttpRequest.getCookiesMap().size());
        assertEquals(1, defaultHttpRequest.getCookies(name).size());
        assertEquals(name, defaultHttpRequest.getCookiesMap().keySet().iterator().next());
        assertEquals(value, defaultHttpRequest.getCookiesMap().values().iterator().next().iterator().next().value());
        defaultHttpRequest.cookie(name, otherValue);
        assertEquals(1, defaultHttpRequest.getCookiesMap().size());
        assertEquals(2, defaultHttpRequest.getCookies(name).size());
        assertEquals(name, defaultHttpRequest.getCookiesMap().keySet().iterator().next());
        List<Cookie> cookies = defaultHttpRequest.getCookies(name);
        List<String> values = new ArrayList<>();
        for (Cookie cookie : cookies) {
            values.add(cookie.value());
        }
        assertTrue(values.contains(value));
        assertTrue(values.contains(otherValue));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.getCookiesMap().put(name, new ArrayList<>()));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.getCookies(name).add(new CookieImpl("aaa", "aaa")));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.getCookiesMap().put(noValueName, new ArrayList<>()));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.removeCookies(noValueName).add(new CookieImpl("aaa", "aaa")));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.getCookies(noValueName).add(new CookieImpl("aaa", "aaa")));


        cookies = defaultHttpRequest.removeCookies(name);
        List<Cookie> finalCookies = cookies;
        assertThrows(UnsupportedOperationException.class, () -> finalCookies.add(new CookieImpl("aaa", "aaa")));
        values = new ArrayList<>();
        for (Cookie cookie : cookies) {
            values.add(cookie.value());
        }
        assertTrue(values.contains(value));
        assertTrue(values.contains(otherValue));

        assertEquals(0, defaultHttpRequest.getCookiesMap().size());
        assertEquals(0, defaultHttpRequest.getCookies(name).size());

        defaultHttpRequest.headers().add(HttpHeaderNames.COOKIE, name + "=" + value + "aaaa;");
        defaultHttpRequest.cookie(new CookieImpl(name, value));
        defaultHttpRequest.cookie(new CookieImpl(name, otherValue));
        defaultHttpRequest.cookie(new CookieImpl(otherName, value));
        defaultHttpRequest.cookie(new CookieImpl(otherName, otherValue));
        assertThrows(UnsupportedOperationException.class, () -> defaultHttpRequest.getCookies(name).add(new CookieImpl("aaa", "aaa")));
        cookies = defaultHttpRequest.removeCookies(name);
        values = new ArrayList<>();
        for (Cookie cookie : cookies) {
            values.add(cookie.value());
        }
        assertTrue(values.contains(value));
        assertTrue(values.contains(otherValue));
        assertTrue(values.contains(value + "aaaa"));
        assertEquals(1, defaultHttpRequest.getCookiesMap().size());
        assertEquals(2, defaultHttpRequest.getCookies(otherName).size());

        defaultHttpRequest.headers().add(HttpHeaderNames.COOKIE, otherName + "=" + value + "aaaa;");
        assertEquals(1, defaultHttpRequest.getCookiesMap().size());
        assertEquals(3, defaultHttpRequest.getCookies(otherName).size());
        cookies = defaultHttpRequest.removeCookies(otherName);
        assertEquals(3, cookies.size());
        assertEquals(0, defaultHttpRequest.getCookiesMap().size());
        assertEquals(0, defaultHttpRequest.getCookies(otherName).size());
        assertNull(defaultHttpRequest.headers().get(HttpHeaderNames.COOKIE));

        assertEquals(0, defaultHttpRequest.removeCookies(null).size());
    }
}
