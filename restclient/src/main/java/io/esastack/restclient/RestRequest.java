package io.esastack.restclient;

import esa.commons.http.Cookie;
import io.esastack.httpclient.core.Request;

import java.util.List;
import java.util.Map;

public interface RestRequest extends Request {

    @Override
    RestRequest addParam(String name, String value);

    @Override
    RestRequest addParams(Map<String, String> params);

    @Override
    RestRequest addHeader(CharSequence name, CharSequence value);

    @Override
    RestRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestRequest setHeader(CharSequence name, CharSequence value);

    @Override
    RestRequest removeHeader(CharSequence name);

    RestRequest cookie(Cookie cookie);

    RestRequest cookie(String name, String value);

    RestRequest cookies(List<Cookie> cookies);

    RestRequest contentType(ContentType contentType);

    RestRequest accept(AcceptType... acceptTypes);

    List<Cookie> removeCookies(String name);

    List<Cookie> cookies(String name);

    Map<String, List<Cookie>> cookiesMap();

    ContentType contentType();

    AcceptType[] acceptTypes();
}
