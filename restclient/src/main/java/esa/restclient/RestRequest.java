package esa.restclient;

import esa.commons.http.Cookie;
import esa.httpclient.core.Request;
import esa.httpclient.core.RequestConfig;

import java.util.List;
import java.util.Map;

public interface RestRequest extends Request, RequestConfig {

    @Override
    RestRequest readTimeout(int readTimeout);

    @Override
    RestRequest maxRedirects(int maxRedirects);

    @Override
    RestRequest maxRetries(int maxRetries);

    @Override
    RestRequest disableExpectContinue();

    @Override
    RestRequest enableUriEncode();

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

    List<Cookie> removeCookies(String name);

    List<Cookie> getCookies(String name);

    Map<String, List<Cookie>> getCookiesMap();

    RestRequest contentType(ContentType contentType);

    RestRequest contentType(RequestContentTypeFactory requestContentTypeFactory);

    ContentType contentType();

    RestRequest accept(ContentType... contentTypes);

    List<ContentType> acceptTypes();

    RestRequest acceptTypeResolver(ResponseContentTypeResolver responseContentTypeResolver);

    Object body();

    RequestContext context();
}
