package io.esastack.restclient;

import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.MediaType;
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

    RestRequest cookie(String name, String value);

    RestRequest cookie(Cookie... cookies);

    RestRequest contentType(MediaType contentType);

    RestRequest accept(MediaType... acceptTypes);

    List<Cookie> removeCookies(String name);

    List<Cookie> cookies(String name);

    Map<String, List<Cookie>> cookiesMap();

    MediaType contentType();
}
