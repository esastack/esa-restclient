package io.esastack.restclient;

import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.Encoder;

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
    ExecutableRestRequest cookie(String name, String value);

    @Override
    ExecutableRestRequest cookie(Cookie... cookies);

    @Override
    ExecutableRestRequest contentType(MediaType contentType);

    @Override
    ExecutableRestRequest accept(MediaType... acceptTypes);

    @Override
    ExecutableRestRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    ExecutableRestRequest addHeader(CharSequence name, CharSequence value);

    @Override
    ExecutableRestRequest setHeader(CharSequence name, CharSequence value);

    @Override
    ExecutableRestRequest removeHeader(CharSequence name);

    @Override
    ExecutableRestRequest encoder(Encoder encoder);

    @Override
    ExecutableRestRequest decoder(Decoder decoder);

}
