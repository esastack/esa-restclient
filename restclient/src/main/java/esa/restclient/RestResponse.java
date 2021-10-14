package esa.restclient;

import esa.commons.http.Cookie;
import esa.httpclient.core.Response;

import java.util.List;
import java.util.Map;

public interface RestResponse extends Response {

    List<Cookie> getCookies(String name);

    Map<String, List<Cookie>> getCookiesMap();
}
