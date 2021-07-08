package esa.restclient;

import esa.commons.http.Cookie;

import java.io.File;
import java.util.Map;

public interface FacadeRequest extends ExecutableRequest {

    EntityRequest entity(Object entity);

    FileRequest file(File file);

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
    FacadeRequest contentType(ContentType contentType);

    @Override
    FacadeRequest contentType(ContentTypeFactory contentTypeFactory);

    @Override
    FacadeRequest accept(AcceptType... acceptTypes);

    @Override
    FacadeRequest accept(AcceptTypeFactory acceptTypeFactory);

    @Override
    FacadeRequest acceptResolver(AcceptTypeResolver acceptTypeResolver);

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

}
