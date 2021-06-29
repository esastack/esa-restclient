package esa.restclient;

import esa.commons.http.Cookie;
import esa.commons.http.HttpVersion;
import esa.commons.netty.http.CookieImpl;
import esa.httpclient.core.config.RetryOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class DefaultFacadeRequestTest {

    private final static HttpVersion httpVersion = HttpVersion.HTTP_1_0;
    private final static String httpUrl = "http://localhost:8080";

    @Test
    void testBasicFunction() {
        FacadeRequest facadeRequest = RestClient.create().version(httpVersion).build().post(httpUrl);
        DefaultHttpRequestTest.testHeaderOperate(facadeRequest);
        DefaultHttpRequestTest.testHeadersOperate(facadeRequest);
        DefaultHttpRequestTest.testContentTypeOperate(facadeRequest);
        DefaultHttpRequestTest.testAcceptOperate(facadeRequest);
        DefaultHttpRequestTest.testHttpVersion(facadeRequest, httpVersion);
        DefaultExecutableRequestTest.testPropertyOperate(facadeRequest);
        DefaultHttpRequestTest.testCookieOperate(facadeRequest);
        DefaultHttpRequestTest.testParamOperate(facadeRequest);
        facadeRequest = RestClient.create().version(httpVersion).build().post(httpUrl);
        DefaultHttpRequestTest.testParamsOperate(facadeRequest);
    }

    @Test
    void testEntityRequestData() {
        FacadeRequest facadeRequest = createFacadeRequest();
        String entity = "entity";
        EntityRequest entityRequest = facadeRequest.bodyEntity(entity);
        testRequestDataEquals(facadeRequest, entityRequest);
        assertEquals(entity, entityRequest.entity());
    }

    @Test
    void testFileRequestData() {
        FacadeRequest facadeRequest = createFacadeRequest();
        File file = new File("entity");
        FileRequest fileRequest = facadeRequest.bodyFile(file);
        testRequestDataEquals(facadeRequest, fileRequest);
        assertEquals(file, fileRequest.file());
    }

    @Test
    void testMultipartRequestData() {
        FacadeRequest facadeRequest = createFacadeRequest();
        MultipartRequest multipartRequest = facadeRequest.multipart();
        testRequestDataEquals(facadeRequest, multipartRequest);
    }

    static FacadeRequest createFacadeRequest() {
        return RestClient.create().version(httpVersion).build().post(httpUrl)
                .addHeader("aaa", "bbb")
                .setHeader("aaa", "ccc")
                .addHeaders(null)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.MULTIPART_FORM_DATA)
                .addParam("aaa", "aaa")
                .addParams(null)
                .cookie("aaa", "bbb")
                .cookie(new CookieImpl("aaa", "aaa"))
                .disableExpectContinue()
                .enableUriEncode()
                .maxRedirects(1)
                .maxRetries(1)
                .readTimeout(1)
                .enableUriEncode()
                .property("aaa", "aaaaaa");
    }

    private void testRequestDataEquals(ExecutableRequest executableRequest, ExecutableRequest otherExecutableRequest) {
        assertNotNull(executableRequest, "ExecutableRequest can,t be null!");
        assertNotNull(otherExecutableRequest, "ExecutableRequest can,t be null!");
        //Request基本参数
        assertEquals(executableRequest.scheme(), otherExecutableRequest.scheme());
        assertEquals(executableRequest.method(), otherExecutableRequest.method());
        assertEquals(executableRequest.uri(), otherExecutableRequest.uri());
        assertEquals(executableRequest.uriEncode(), otherExecutableRequest.uriEncode());
        assertEquals(executableRequest.paramNames(), otherExecutableRequest.paramNames());
        for (String name : executableRequest.paramNames()) {
            assertEquals(executableRequest.getParam(name), otherExecutableRequest.getParam(name));
        }

        //Header相关
        Assertions.assertEquals(executableRequest.headers(), otherExecutableRequest.headers());
        Assertions.assertEquals(executableRequest.headers().names().toString(), otherExecutableRequest.headers().names().toString());
        for (String name : executableRequest.headers().names()) {
            assertEquals(executableRequest.getHeaders(name), otherExecutableRequest.getHeaders(name));
        }
        Assertions.assertEquals(executableRequest.contentType(), otherExecutableRequest.contentType());
        assertEquals(executableRequest.acceptTypes(), otherExecutableRequest.acceptTypes());

        //Cookie
        assertEquals(executableRequest.getCookiesMap().size(), otherExecutableRequest.getCookiesMap().size());
        for (String name : executableRequest.getCookiesMap().keySet()) {
            assertEquals(executableRequest.getCookies(name).size(), otherExecutableRequest.getCookies(name).size());
            for (int i = 0; i < executableRequest.getCookies(name).size(); i++) {
                Cookie cookie = executableRequest.getCookies(name).get(i);
                Cookie otherCookie = otherExecutableRequest.getCookies(name).get(i);
                assertEquals(cookie.name(), otherCookie.name());
                assertEquals(cookie.value(), otherCookie.value());
                assertEquals(cookie.domain(), otherCookie.domain());
                assertEquals(cookie.isHttpOnly(), otherCookie.isHttpOnly());
                assertEquals(cookie.maxAge(), otherCookie.maxAge());
                assertEquals(cookie.isSecure(), otherCookie.isSecure());
                assertEquals(cookie.wrap(), otherCookie.wrap());
            }
        }

        //properties相关
        assertEquals(executableRequest.properties(), otherExecutableRequest.properties());
        assertEquals(executableRequest.properties().entrySet(), otherExecutableRequest.properties().entrySet());

        //超时等执行参数
        assertEquals(executableRequest.readTimeout(), otherExecutableRequest.readTimeout());
        assertEquals(executableRequest.maxRedirects(), otherExecutableRequest.maxRedirects());
        assertEquals(executableRequest.maxRetries(), otherExecutableRequest.maxRetries());
    }

    @Test
    void testReadTimeout() {
        RestClientBuilder builder = RestClient.create();
        builder.version(HttpVersion.HTTP_1_0);
        assertThrows(IllegalArgumentException.class, () -> builder.readTimeout(0));
        assertThrows(IllegalArgumentException.class, () -> builder.readTimeout(-1));
        int builderReadTimeout = 5;
        builder.readTimeout(builderReadTimeout);
        assertEquals(builderReadTimeout, builder.readTimeout());
        FacadeRequest defaultFacadeRequest = RestClient.create().readTimeout(builderReadTimeout).version(httpVersion).build().post(httpUrl);
        assertEquals(builderReadTimeout, defaultFacadeRequest.readTimeout());

        assertThrows(IllegalArgumentException.class, () -> defaultFacadeRequest.readTimeout(0));
        assertThrows(IllegalArgumentException.class, () -> defaultFacadeRequest.readTimeout(-1));
        int requestReadTimeout = 3;
        defaultFacadeRequest.readTimeout(requestReadTimeout);
        assertEquals(requestReadTimeout, defaultFacadeRequest.readTimeout());
        requestReadTimeout = 1;
        defaultFacadeRequest.readTimeout(requestReadTimeout);
        assertEquals(requestReadTimeout, defaultFacadeRequest.readTimeout());
        requestReadTimeout = 2;
        defaultFacadeRequest.readTimeout(requestReadTimeout);
        assertEquals(requestReadTimeout, defaultFacadeRequest.readTimeout());
        assertThrows(IllegalArgumentException.class, () -> defaultFacadeRequest.readTimeout(0));
        assertThrows(IllegalArgumentException.class, () -> defaultFacadeRequest.readTimeout(-1));
        assertEquals(requestReadTimeout, defaultFacadeRequest.readTimeout());
        assertEquals(builderReadTimeout, builder.readTimeout());
    }

    @Test
    void testMaxRedirects() {
        RestClientBuilder builder = RestClient.create();
        builder.version(HttpVersion.HTTP_1_0);
        assertThrows(IllegalArgumentException.class, () -> builder.maxRedirects(-1));
        int builderMaxRedirects = 5;
        builder.maxRedirects(builderMaxRedirects);
        assertEquals(builderMaxRedirects, builder.maxRedirects());
        FacadeRequest defaultFacadeRequest = RestClient.create().version(httpVersion).build().post(httpUrl);
        assertEquals(builderMaxRedirects, defaultFacadeRequest.maxRedirects());

        assertThrows(IllegalArgumentException.class, () -> defaultFacadeRequest.maxRedirects(-1));
        int requestMaxRedirects = 3;
        defaultFacadeRequest.maxRedirects(requestMaxRedirects);
        assertEquals(requestMaxRedirects, defaultFacadeRequest.maxRedirects());
        requestMaxRedirects = 2;
        defaultFacadeRequest.maxRedirects(requestMaxRedirects);
        assertEquals(requestMaxRedirects, defaultFacadeRequest.maxRedirects());
        assertThrows(IllegalArgumentException.class, () -> defaultFacadeRequest.maxRedirects(-1));
        assertEquals(requestMaxRedirects, defaultFacadeRequest.maxRedirects());
        assertEquals(builderMaxRedirects, builder.maxRedirects());
    }

    @Test
    void testMaxRetries() {
        FacadeRequest defaultFacadeRequestWithoutRetry = RestClient.create().retryOptions(null).version(httpVersion).build().post(httpUrl);
        assertEquals(0, defaultFacadeRequestWithoutRetry.maxRetries());

        FacadeRequest defaultFacadeRequest = RestClient.create().retryOptions(RetryOptions.ofDefault()).version(httpVersion).build().post(httpUrl);
        assertEquals(RetryOptions.ofDefault().maxRetries(), defaultFacadeRequest.maxRetries());

        assertThrows(IllegalArgumentException.class, () -> defaultFacadeRequest.maxRetries(-1));
        assertThrows(IllegalArgumentException.class, () -> defaultFacadeRequest.maxRetries(0));

        int requestMaxRetries = 10;
        defaultFacadeRequest.maxRetries(requestMaxRetries);
        assertEquals(requestMaxRetries, defaultFacadeRequest.maxRetries());
        requestMaxRetries = 2;
        defaultFacadeRequest.maxRetries(requestMaxRetries);
        assertEquals(requestMaxRetries, defaultFacadeRequest.maxRetries());
        assertThrows(IllegalArgumentException.class, () -> defaultFacadeRequest.maxRetries(-1));
        assertEquals(requestMaxRetries, defaultFacadeRequest.maxRetries());
        assertEquals(RetryOptions.ofDefault().maxRetries(), RetryOptions.ofDefault().maxRetries());
    }

}
