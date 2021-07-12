package esa.restclient;

import esa.commons.http.Cookie;

import java.io.File;
import java.util.List;
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
    FacadeRestRequest cookies(List<Cookie> cookies);

    @Override
    FacadeRestRequest contentType(ContentType contentType);

    @Override
    FacadeRestRequest contentType(RequestContentTypeFactory requestContentTypeFactory);

    @Override
    FacadeRestRequest accept(ContentType... contentTypes);


    @Override
    FacadeRestRequest acceptTypeResolver(ResponseContentTypeResolver responseContentTypeResolver);

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
