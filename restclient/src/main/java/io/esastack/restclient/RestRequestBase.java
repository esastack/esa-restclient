package io.esastack.restclient;

import esa.commons.http.Cookie;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.RequestBaseConfigure;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.Encoder;

import java.util.List;
import java.util.Map;

public interface RestRequestBase extends RestRequest, RequestBaseConfigure {
    Object entity();

    @Override
    RestRequestBase readTimeout(long readTimeout);

    @Override
    RestRequestBase maxRedirects(int maxRedirects);

    @Override
    RestRequestBase maxRetries(int maxRetries);

    @Override
    RestRequestBase disableExpectContinue();

    @Override
    RestRequestBase enableUriEncode();

    @Override
    RestRequestBase addParams(Map<String, String> params);

    @Override
    RestRequestBase addParam(String name, String value);

    @Override
    RestRequestBase addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestRequestBase addHeader(CharSequence name, CharSequence value);

    @Override
    RestRequestBase setHeader(CharSequence name, CharSequence value);

    @Override
    RestRequestBase removeHeader(CharSequence name);

    @Override
    RestRequestBase cookie(Cookie cookie);

    @Override
    RestRequestBase cookie(String name, String value);

    @Override
    RestRequestBase cookies(List<Cookie> cookies);

    @Override
    RestRequestBase contentType(MediaType contentType);

    @Override
    RestRequestBase accept(MediaType... acceptTypes);

    RestRequestBase encoder(Encoder encoder);

    Encoder encoder();

    RestRequestBase decoder(Decoder decoder);

    Decoder decoder();
}
