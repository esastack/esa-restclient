package esa.restclient;

import esa.commons.http.Cookie;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface RestFileRequest extends ExecutableRestRequest {

    File file();

    @Override
    RestFileRequest addParams(Map<String, String> params);

    @Override
    RestFileRequest addParam(String name, String value);

    @Override
    RestFileRequest cookie(Cookie cookie);

    @Override
    RestFileRequest cookie(String name, String value);

    @Override
    RestFileRequest cookies(List<Cookie> cookies);

    @Override
    RestFileRequest contentType(ContentType contentType);

    @Override
    RestFileRequest contentType(ContentTypeProvider contentTypeProvider);

    @Override
    RestFileRequest accept(ContentType... contentTypes);

    @Override
    RestFileRequest contentTypeResolver(ContentTypeResolver contentTypeResolver);

    @Override
    RestFileRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestFileRequest addHeader(CharSequence name, CharSequence value);

    @Override
    RestFileRequest setHeader(CharSequence name, CharSequence value);

    @Override
    RestFileRequest removeHeader(CharSequence name);

    @Override
    RestFileRequest enableUriEncode();

    @Override
    RestFileRequest readTimeout(int readTimeout);

    @Override
    RestFileRequest disableExpectContinue();

    @Override
    RestFileRequest maxRedirects(int maxRedirects);

    @Override
    RestFileRequest maxRetries(int maxRetries);

}
