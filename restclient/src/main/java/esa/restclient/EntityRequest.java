package esa.restclient;

import esa.commons.http.Cookie;

import java.util.Map;

public interface EntityRequest extends ExecutableRequest{

    Object entity();

    @Override
    EntityRequest addParams(Map<String, String> params);

    @Override
    EntityRequest addParam(String name, String value);

    @Override
    EntityRequest cookie(Cookie cookie);

    @Override
    EntityRequest cookie(String name, String value);

    @Override
    EntityRequest contentType(ContentType contentType);

    @Override
    EntityRequest contentType(ContentTypeResolver contentTypeResolver);

    @Override
    EntityRequest accept(AcceptType... acceptTypes);

    @Override
    EntityRequest accept(AcceptTypeResolver acceptTypeResolver);


    @Override
    EntityRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    EntityRequest addHeader(CharSequence name, CharSequence value);

    @Override
    EntityRequest setHeader(CharSequence name, CharSequence value);

    @Override
    EntityRequest enableUriEncode();

    @Override
    EntityRequest readTimeout(int readTimeout);

    @Override
    EntityRequest disableExpectContinue();

    @Override
    EntityRequest maxRedirects(int maxRedirects);

    @Override
    EntityRequest maxRetries(int maxRetries);

    @Override
    EntityRequest property(String name, Object value);
}
