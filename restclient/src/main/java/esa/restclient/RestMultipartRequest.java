package esa.restclient;

import esa.commons.http.Cookie;
import esa.httpclient.core.MultipartConfig;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface RestMultipartRequest extends ExecutableRestRequest, MultipartConfig {

    @Override
    RestMultipartRequest multipartEncode(boolean multipartEncode);

    @Override
    RestMultipartRequest attr(String name, String value);

    @Override
    RestMultipartRequest file(String name, File file);

    @Override
    RestMultipartRequest file(String name, File file, String contentType);

    @Override
    RestMultipartRequest file(String name, File file, String contentType, boolean isText);

    @Override
    RestMultipartRequest file(String name, String filename, File file, String contentType, boolean isText);

    @Override
    RestMultipartRequest addParams(Map<String, String> params);

    @Override
    RestMultipartRequest addParam(String name, String value);

    @Override
    RestMultipartRequest cookie(Cookie cookie);

    @Override
    RestMultipartRequest cookie(String name, String value);

    @Override
    RestMultipartRequest cookies(List<Cookie> cookies);

    @Override
    RestMultipartRequest contentType(ContentType contentType);

    @Override
    RestMultipartRequest contentType(ContentTypeProvider contentTypeProvider);

    @Override
    RestMultipartRequest accept(ContentType... contentTypes);

    @Override
    RestMultipartRequest contentTypeResolver(ContentTypeResolver contentTypeResolver);

    @Override
    RestMultipartRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestMultipartRequest addHeader(CharSequence name, CharSequence value);

    @Override
    RestMultipartRequest setHeader(CharSequence name, CharSequence value);

    @Override
    RestMultipartRequest removeHeader(CharSequence name);

    @Override
    RestMultipartRequest enableUriEncode();

    @Override
    RestMultipartRequest readTimeout(int readTimeout);

    @Override
    RestMultipartRequest disableExpectContinue();

    @Override
    RestMultipartRequest maxRedirects(int maxRedirects);

    @Override
    RestMultipartRequest maxRetries(int maxRetries);

}
