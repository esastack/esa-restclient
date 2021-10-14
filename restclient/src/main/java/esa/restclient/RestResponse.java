package esa.restclient;

import esa.commons.http.Cookie;
import esa.httpclient.core.Response;

import java.util.List;
import java.util.Map;

public interface RestResponse extends Response {

    void cookie(Cookie cookie);

    void cookie(String name, String value);

    void cookies(List<Cookie> cookies);

    List<Cookie> removeCookies(String name);

    List<Cookie> getCookies(String name);

    Map<String, List<Cookie>> getCookiesMap();
}
