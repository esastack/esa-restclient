package esa.restclient;

import esa.commons.http.Cookie;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface RestRequestFacade extends ExecutableRestRequest {

    RestEntityRequest entity(Object entity);

    RestEntityRequest entity(String content);

    RestEntityRequest entity(byte[] data);

    RestFileRequest entity(File file);

    RestMultipartRequest multipart();

    @Override
    RestRequestFacade addParams(Map<String, String> params);

    @Override
    RestRequestFacade addParam(String name, String value);

    @Override
    RestRequestFacade cookie(Cookie cookie);

    @Override
    RestRequestFacade cookie(String name, String value);

    @Override
    RestRequestFacade cookies(List<Cookie> cookies);

    @Override
    RestRequestFacade contentType(ContentType contentType);

    @Override
    RestRequestFacade accept(ContentType... contentTypes);

    @Override
    RestRequestFacade addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestRequestFacade addHeader(CharSequence name, CharSequence value);

    @Override
    RestRequestFacade setHeader(CharSequence name, CharSequence value);

    @Override
    RestRequestFacade removeHeader(CharSequence name);

    @Override
    RestRequestFacade enableUriEncode();

    @Override
    RestRequestFacade readTimeout(int readTimeout);

    @Override
    RestRequestFacade disableExpectContinue();

    @Override
    RestRequestFacade maxRedirects(int maxRedirects);

    @Override
    RestRequestFacade maxRetries(int maxRetries);

}
