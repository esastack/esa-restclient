package esa.restclient;

import esa.commons.http.Cookie;

import java.io.File;
import java.util.Map;

public interface FacadeRequest extends ExecutableRequest {

    EntityRequest bodyEntity(Object entity);

    EntityRequest bodyEntity(Object entity, MediaType mediaType);

    FileRequest bodyFile(File file);

    FileRequest bodyFile(File file, MediaType mediaType);

    MultipartRequest multipart();

    @Override
    FacadeRequest addParams(Map<String, String> params);

    @Override
    FacadeRequest addParam(String name, String value);

    @Override
    FacadeRequest cookie(Cookie cookie);

    @Override
    FacadeRequest cookie(String name, String value);

    @Override
    FacadeRequest accept(MediaType... mediaTypes);

    @Override
    FacadeRequest contentType(MediaType mediaType);

    @Override
    FacadeRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    FacadeRequest addHeader(CharSequence name, CharSequence value);

    @Override
    FacadeRequest setHeader(CharSequence name, CharSequence value);

    @Override
    FacadeRequest enableUriEncode();

    @Override
    FacadeRequest readTimeout(int readTimeout);

    @Override
    FacadeRequest disableExpectContinue();

    @Override
    FacadeRequest maxRedirects(int maxRedirects);

    @Override
    FacadeRequest maxRetries(int maxRetries);

    @Override
    FacadeRequest property(String name, Object value);

}
