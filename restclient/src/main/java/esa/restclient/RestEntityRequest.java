package esa.restclient;

import esa.commons.http.Cookie;

import java.util.List;
import java.util.Map;

public interface RestEntityRequest extends ExecutableRestRequest {

    Object entity();

    @Override
    RestEntityRequest addParams(Map<String, String> params);

    @Override
    RestEntityRequest addParam(String name, String value);

    @Override
    RestEntityRequest cookie(Cookie cookie);

    @Override
    RestEntityRequest cookie(String name, String value);

    @Override
    RestEntityRequest cookies(List<Cookie> cookies);

    @Override
    RestEntityRequest contentType(ContentType contentType);

    @Override
    RestEntityRequest contentType(ContentTypeProvider contentTypeProvider);

    @Override
    RestEntityRequest accept(ContentType... contentTypes);

    @Override
    RestEntityRequest contentTypeResolver(ContentTypeResolver contentTypeResolver);

    @Override
    RestEntityRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestEntityRequest addHeader(CharSequence name, CharSequence value);

    @Override
    RestEntityRequest setHeader(CharSequence name, CharSequence value);

    @Override
    RestEntityRequest removeHeader(CharSequence name);

    @Override
    RestEntityRequest enableUriEncode();

    @Override
    RestEntityRequest readTimeout(int readTimeout);

    @Override
    RestEntityRequest disableExpectContinue();

    @Override
    RestEntityRequest maxRedirects(int maxRedirects);

    @Override
    RestEntityRequest maxRetries(int maxRetries);
}
