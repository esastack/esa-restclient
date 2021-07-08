package esa.restclient;

import esa.commons.http.Cookie;

import java.io.File;
import java.util.Map;

public interface FacadeRestRequest extends ExecutableRestRequest {

    RestEntityRequest entity(Object entity);

    RestFileRequest file(File file);

    RestMultipartRequest multipart();

    @Override
    FacadeRestRequest addParams(Map<String, String> params);

    @Override
    FacadeRestRequest addParam(String name, String value);

    @Override
    FacadeRestRequest cookie(Cookie cookie);

    @Override
    FacadeRestRequest cookie(String name, String value);

    @Override
    FacadeRestRequest contentType(ContentType contentType);

    @Override
    FacadeRestRequest contentType(ContentTypeFactory contentTypeFactory);

    @Override
    FacadeRestRequest accept(AcceptType... acceptTypes);

    @Override
    FacadeRestRequest accept(AcceptTypeFactory acceptTypeFactory);

    @Override
    FacadeRestRequest acceptResolver(AcceptTypeResolver acceptTypeResolver);

    @Override
    FacadeRestRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    FacadeRestRequest addHeader(CharSequence name, CharSequence value);

    @Override
    FacadeRestRequest setHeader(CharSequence name, CharSequence value);

    @Override
    FacadeRestRequest removeHeader(CharSequence name);

    @Override
    FacadeRestRequest enableUriEncode();

    @Override
    FacadeRestRequest readTimeout(int readTimeout);

    @Override
    FacadeRestRequest disableExpectContinue();

    @Override
    FacadeRestRequest maxRedirects(int maxRedirects);

    @Override
    FacadeRestRequest maxRetries(int maxRetries);

}