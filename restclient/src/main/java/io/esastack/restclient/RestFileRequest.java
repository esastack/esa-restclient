package io.esastack.restclient;

import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.Encoder;

import java.io.File;
import java.util.Map;

public interface RestFileRequest extends ExecutableRestRequest {

    File file();

    @Override
    RestFileRequest addParams(Map<String, String> params);

    @Override
    RestFileRequest addParam(String name, String value);

    @Override
    RestFileRequest cookie(String name, String value);

    @Override
    RestFileRequest cookie(Cookie... cookies);

    @Override
    RestFileRequest contentType(MediaType contentType);

    @Override
    RestFileRequest accept(MediaType... acceptTypes);

    @Override
    RestFileRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestFileRequest addHeader(CharSequence name, CharSequence value);

    @Override
    RestFileRequest setHeader(CharSequence name, CharSequence value);

    @Override
    RestFileRequest removeHeader(CharSequence name);

    @Override
    RestFileRequest enableUriEncode();

    @Override
    RestFileRequest readTimeout(long readTimeout);

    @Override
    RestFileRequest disableExpectContinue();

    @Override
    RestFileRequest maxRedirects(int maxRedirects);

    @Override
    RestFileRequest maxRetries(int maxRetries);

    @Override
    RestFileRequest encoder(Encoder encoder);

    @Override
    RestFileRequest decoder(Decoder decoder);

}
