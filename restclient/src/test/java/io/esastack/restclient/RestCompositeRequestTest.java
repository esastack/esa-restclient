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
package io.esastack.restclient;

import com.alibaba.fastjson.TypeReference;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.commons.net.netty.http.CookieImpl;
import io.esastack.httpclient.core.MultipartBody;
import io.esastack.httpclient.core.MultipartBodyImpl;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.Encoder;
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
        request.setHeader("bbb", "bbb2");
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

        //test set cookie by cookie(name , value)
        assertThrows(NullPointerException.class, () -> request.cookie("aaa", null));
        assertThrows(NullPointerException.class, () -> request.cookie(null, "aaa"));
        request.cookie("aaa", "aaa1");
        request.cookie("aaa", "aaa2");

        //test set cookie by cookie(cookie)
        assertDoesNotThrow(() ->
                request.cookie(null)
        );
        request.cookie(new CookieImpl("bbb", "bbb1"));
        request.cookie(new CookieImpl("bbb", "bbb2"));
        request.cookie(new CookieImpl("ccc", "ccc1"), new CookieImpl("ccc", "ccc2"));

        //test get cookies
        then(request.cookiesMap().size()).isEqualTo(3);
        then(request.cookies("aaa").size()).isEqualTo(2);
        then(request.cookies("aaa").get(0).value()).isEqualTo("aaa1");
        then(request.cookies("aaa").get(1).value()).isEqualTo("aaa2");

        then(request.cookies("bbb").size()).isEqualTo(2);
        then(request.cookies("bbb").get(0).value()).isEqualTo("bbb1");
        then(request.cookies("bbb").get(1).value()).isEqualTo("bbb2");

        then(request.cookies("ccc").size()).isEqualTo(2);
        then(request.cookies("ccc").get(0).value()).isEqualTo("ccc1");
        then(request.cookies("ccc").get(1).value()).isEqualTo("ccc2");

        then(request.headers().getAll(HttpHeaderNames.COOKIE).size()).isEqualTo(6);

        //test remove cookies
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

        //test remove cookies when the name of cookie is null
        cookies = request.removeCookies(null);
        then(cookies.size()).isEqualTo(0);
        then(request.cookiesMap().get("bbb").size()).isEqualTo(2);
        then(request.cookiesMap().get("bbb").get(0).value()).isEqualTo("bbb1");
        then(request.cookiesMap().get("bbb").get(1).value()).isEqualTo("bbb2");

        then(request.cookiesMap().get("ccc").size()).isEqualTo(2);
        then(request.cookiesMap().get("ccc").get(0).value()).isEqualTo("ccc1");
        then(request.cookiesMap().get("ccc").get(1).value()).isEqualTo("ccc2");

        then(request.headers().getAll(HttpHeaderNames.COOKIE).size()).isEqualTo(4);

        //test remove cookie by cookiesMap().remove(name)
        assertThrows(UnsupportedOperationException.class, () -> request.cookiesMap().remove("aaa"));

    }

    @Test
    void testContentType() {
        RestRequestFacade request = RestClient.ofDefault().post("aaa");
        File file = new File("aaa");
        RestFileRequest restFileRequest = request.entity(file);
        then(restFileRequest.file()).isEqualTo(file);
        then(request.contentType()).isEqualTo(MediaTypeUtil.APPLICATION_OCTET_STREAM);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(MediaTypeUtil.APPLICATION_OCTET_STREAM.value());
        request.contentType(MediaTypeUtil.APPLICATION_JSON_UTF8);
        then(request.contentType()).isEqualTo(MediaTypeUtil.APPLICATION_JSON_UTF8);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(MediaTypeUtil.APPLICATION_JSON_UTF8.value());

        RestRequestFacade finalRequest = request;
        assertThrows(IllegalStateException.class, () ->
                finalRequest.entity(new Object())
        );

        request = RestClient.ofDefault().post("aaa");
        Object entity = new Object();
        request.entity(entity);
        then(request.entity()).isEqualTo(entity);
        then(request.contentType()).isEqualTo(MediaTypeUtil.APPLICATION_JSON_UTF8);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(MediaTypeUtil.APPLICATION_JSON_UTF8.value());
        request.contentType(MediaTypeUtil.APPLICATION_FORM_URLENCODED);
        then(request.contentType()).isEqualTo(MediaTypeUtil.APPLICATION_FORM_URLENCODED);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(MediaTypeUtil.APPLICATION_FORM_URLENCODED.value());
        RestRequestFacade finalRequest1 = request;
        assertThrows(IllegalStateException.class, () ->
                finalRequest1.entity(new Object())
        );

        request = RestClient.ofDefault().post("aaa");
        String aaa = "aaa";
        request.entity(aaa);
        then(request.entity()).isEqualTo(aaa);
        then(request.contentType()).isEqualTo(MediaTypeUtil.TEXT_PLAIN);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(MediaTypeUtil.TEXT_PLAIN.value());
        request.contentType(MediaTypeUtil.APPLICATION_FORM_URLENCODED);
        then(request.contentType()).isEqualTo(MediaTypeUtil.APPLICATION_FORM_URLENCODED);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(MediaTypeUtil.APPLICATION_FORM_URLENCODED.value());
        RestRequestFacade finalRequest2 = request;
        assertThrows(IllegalStateException.class, () ->
                finalRequest2.entity(new Object())
        );

        request = RestClient.ofDefault().post("aaa");
        byte[] bytes = "aaa".getBytes();
        request.entity(bytes);
        then(request.entity()).isEqualTo(bytes);
        then(request.contentType()).isEqualTo(MediaTypeUtil.APPLICATION_OCTET_STREAM);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(MediaTypeUtil.APPLICATION_OCTET_STREAM.value());
        request.contentType(MediaTypeUtil.APPLICATION_FORM_URLENCODED);
        then(request.contentType()).isEqualTo(MediaTypeUtil.APPLICATION_FORM_URLENCODED);
        then(request.getHeader(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo(MediaTypeUtil.APPLICATION_FORM_URLENCODED.value());
        RestRequestFacade finalRequest3 = request;
        assertThrows(IllegalStateException.class, () ->
                finalRequest3.entity(new Object())
        );

        assertThrows(NullPointerException.class, () -> finalRequest3.contentType(null));
    }

    @Test
    void testEntity() {
        RestRequestFacade request = RestClient.ofDefault().post("aaa");
        List<String> stringList = new ArrayList<>();
        request.entity(stringList, new TypeReference<List<String>>() {
        }.getType());
        then(request.entity()).isEqualTo(stringList);
        then(request.type()).isEqualTo(ArrayList.class);
        then(request.genericType()).isEqualTo(new TypeReference<List<String>>() {
        }.getType());
        final RestRequestFacade finalRequest1 = request;
        assertThrows(IllegalStateException.class, () ->
                finalRequest1.entity(new Object())
        );

        final RestRequestFacade finalRequest2 = RestClient.ofDefault().post("aaaa");
        assertThrows(IllegalArgumentException.class, () ->
                finalRequest2.entity(stringList, new TypeReference<Map<String, String>>() {
                }.getType())
        );

        request = RestClient.ofDefault().post("aaaa");
        request.entity(stringList);
        then(request.entity()).isEqualTo(stringList);
        then(request.type()).isEqualTo(ArrayList.class);
        then(request.genericType()).isEqualTo(ArrayList.class);

        request = RestClient.ofDefault().post("aaaa");
        String data = "data";
        request.entity(data);
        then(request.entity()).isEqualTo(data);
        then(request.type()).isEqualTo(String.class);
        then(request.genericType()).isEqualTo(String.class);

        request = RestClient.ofDefault().post("aaaa");
        File dataFile = new File("data");
        request.entity(dataFile);
        then(request.entity()).isEqualTo(dataFile);
        then(request.type()).isEqualTo(File.class);
        then(request.genericType()).isEqualTo(File.class);

        request = RestClient.ofDefault().post("aaaa");
        request.multipart();
        then(request.entity()).isInstanceOf(MultipartBody.class);
        then(request.type()).isEqualTo(MultipartBodyImpl.class);
        then(request.genericType()).isEqualTo(MultipartBodyImpl.class);
    }

    @Test
    void testAcceptType() {
        RestRequestFacade request = RestClient.ofDefault().post("aaa");
        assertThrows(NullPointerException.class, () -> request.accept(null));

        then(request.getHeader(HttpHeaderNames.ACCEPT)).isNull();

        request.accept(MediaTypeUtil.APPLICATION_JSON_UTF8);
        then(MediaTypeUtil.APPLICATION_JSON_UTF8.value()).isEqualTo(request.getHeader(HttpHeaderNames.ACCEPT));
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
        then(request.contentType()).isEqualTo(MediaTypeUtil.MULTIPART_FORM_DATA);
        then(request.entity() instanceof MultipartBody).isTrue();

        MultipartBody body = (MultipartBody) request.entity();
        then(body.files().size()).isEqualTo(4);
        then(body.files().get(0).file()).isEqualTo(file);
        then(body.attrs().size()).isEqualTo(1);
        then(body.attrs().getFirst("bbb")).isEqualTo("bbb");
    }

    @Test
    void testSetDecoder() {
        Encoder encoder = encodeContext -> null;
        RestRequestFacade request = RestClient.ofDefault()
                .post("http://localhost:8080/test")
                .readTimeout(9000)
                .maxRedirects(10)
                .maxRetries(10)
                .encoder(encoder);
        then(request.encoder()).isEqualTo(encoder);
    }

    @Test
    void testSetEncoder() {
        Decoder decoder = decodeContext -> null;
        RestRequestFacade request = RestClient.ofDefault()
                .post("http://localhost:8080/test")
                .readTimeout(9000)
                .maxRedirects(10)
                .maxRetries(10)
                .decoder(decoder);
        then(request.decoder()).isEqualTo(decoder);
    }

}
