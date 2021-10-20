package io.esastack.restclient;

import esa.commons.http.Cookie;
import io.esastack.httpclient.core.Response;

import java.util.List;
import java.util.Map;

public interface RestResponse extends Response {

    void cookie(Cookie cookie);

    void cookie(String name, String value);

    void cookies(List<Cookie> cookies);

    List<Cookie> removeCookies(String name);

    List<Cookie> cookies(String name);

    Map<String, List<Cookie>> cookiesMap();
}
