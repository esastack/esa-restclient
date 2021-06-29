package esa.restclient;

import esa.commons.http.Cookie;

import java.util.Map;
import java.util.concurrent.CompletionStage;

public interface ExecutableRequest extends RestHttpRequest {

    CompletionStage<RestHttpResponse> execute();

    ExecutableRequest readTimeout(int readTimeout);

    ExecutableRequest maxRedirects(int maxRedirects);

    ExecutableRequest maxRetries(int maxRetries);

    ExecutableRequest disableExpectContinue();

    ExecutableRequest enableUriEncode();

    @Override
    ExecutableRequest addParams(Map<String, String> params);

    @Override
    ExecutableRequest addParam(String name, String value);

    @Override
    ExecutableRequest cookie(Cookie cookie);

    @Override
    ExecutableRequest cookie(String name, String value);

    @Override
    ExecutableRequest contentType(MediaType mediaType);

    @Override
    ExecutableRequest accept(MediaType... mediaTypes);

    @Override
    ExecutableRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    ExecutableRequest addHeader(CharSequence name, CharSequence value);

    @Override
    ExecutableRequest setHeader(CharSequence name, CharSequence value);

}
