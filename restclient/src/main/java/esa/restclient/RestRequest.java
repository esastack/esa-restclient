package esa.restclient;

import esa.commons.http.Cookie;
import esa.httpclient.core.Request;
import esa.httpclient.core.RequestMoreConfig;

import java.util.List;
import java.util.Map;

public interface RestRequest extends Request, RequestMoreConfig {

    @Override
    RestRequest addParams(Map<String, String> params);

    @Override
    RestRequest addParam(String name, String value);

    @Override
    RestRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestRequest addHeader(CharSequence name, CharSequence value);

    @Override
    RestRequest setHeader(CharSequence name, CharSequence value);

    @Override
    RestRequest removeHeader(CharSequence name);

    RestRequest cookie(Cookie cookie);

    RestRequest cookie(String name, String value);

    RestRequest cookies(List<Cookie> cookies);

    RestRequest contentType(ContentType contentType);

    RestRequest accept(ContentType... contentTypes);

    List<Cookie> removeCookies(String name);

    List<Cookie> getCookies(String name);

    Map<String, List<Cookie>> getCookiesMap();

    ContentType contentType();

    ContentType[] acceptTypes();
}
