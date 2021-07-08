package esa.restclient;

import esa.commons.http.Cookie;

import java.io.File;
import java.util.Map;

public interface FileRequest extends ExecutableRequest{

    File file();

    @Override
    FileRequest addParams(Map<String, String> params);

    @Override
    FileRequest addParam(String name, String value);

    @Override
    FileRequest cookie(Cookie cookie);

    @Override
    FileRequest cookie(String name, String value);

    @Override
    FileRequest contentType(ContentType contentType);

    @Override
    FileRequest contentType(ContentTypeFactory contentTypeFactory);

    @Override
    FileRequest accept(AcceptType... acceptTypes);

    @Override
    FileRequest accept(AcceptTypeFactory acceptTypeFactory);

    @Override
    FileRequest acceptResolver(AcceptTypeResolver acceptTypeResolver);

    @Override
    FileRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    FileRequest addHeader(CharSequence name, CharSequence value);

    @Override
    FileRequest setHeader(CharSequence name, CharSequence value);

    @Override
    FileRequest enableUriEncode();

    @Override
    FileRequest readTimeout(int readTimeout);

    @Override
    FileRequest disableExpectContinue();

    @Override
    FileRequest maxRedirects(int maxRedirects);

    @Override
    FileRequest maxRetries(int maxRetries);

}
