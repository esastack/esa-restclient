package io.esastack.restclient;

import esa.commons.http.Cookie;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public interface ExecutableRestRequest extends RestRequestBase {

    CompletionStage<RestResponseBase> execute();

    @Override
    ExecutableRestRequest readTimeout(long readTimeout);

    @Override
    ExecutableRestRequest maxRedirects(int maxRedirects);

    @Override
    ExecutableRestRequest maxRetries(int maxRetries);

    @Override
    ExecutableRestRequest disableExpectContinue();

    @Override
    ExecutableRestRequest enableUriEncode();

    @Override
    ExecutableRestRequest addParams(Map<String, String> params);

    @Override
    ExecutableRestRequest addParam(String name, String value);

    @Override
    ExecutableRestRequest cookie(Cookie cookie);

    @Override
    ExecutableRestRequest cookie(String name, String value);

    @Override
    ExecutableRestRequest cookies(List<Cookie> cookies);

    @Override
    ExecutableRestRequest contentType(ContentType contentType);

    @Override
    ExecutableRestRequest accept(AcceptType... acceptTypes);

    @Override
    ExecutableRestRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    ExecutableRestRequest addHeader(CharSequence name, CharSequence value);

    @Override
    ExecutableRestRequest setHeader(CharSequence name, CharSequence value);

    @Override
    ExecutableRestRequest removeHeader(CharSequence name);

}