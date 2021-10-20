package esa.restclient;

import esa.commons.http.Cookie;
import esa.httpclient.core.RequestBaseConfigure;

import java.util.List;
import java.util.Map;

public interface RestRequestBase extends RestRequest, RequestBaseConfigure {
    Object entity();

    @Override
    RestRequestBase readTimeout(long readTimeout);

    @Override
    RestRequestBase maxRedirects(int maxRedirects);

    @Override
    RestRequestBase maxRetries(int maxRetries);

    @Override
    RestRequestBase disableExpectContinue();

    @Override
    RestRequestBase enableUriEncode();

    @Override
    RestRequestBase addParams(Map<String, String> params);

    @Override
    RestRequestBase addParam(String name, String value);

    @Override
    RestRequestBase addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestRequestBase addHeader(CharSequence name, CharSequence value);

    @Override
    RestRequestBase setHeader(CharSequence name, CharSequence value);

    @Override
    RestRequestBase removeHeader(CharSequence name);

    RestRequestBase cookie(Cookie cookie);

    RestRequestBase cookie(String name, String value);

    RestRequestBase cookies(List<Cookie> cookies);

    RestRequestBase contentType(ContentType contentType);

    RestRequestBase accept(ContentType... contentTypes);
}
