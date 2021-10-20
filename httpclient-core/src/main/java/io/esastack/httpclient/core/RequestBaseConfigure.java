package io.esastack.httpclient.core;

public interface RequestBaseConfigure {

    RequestBaseConfigure enableUriEncode();

    RequestBaseConfigure disableExpectContinue();

    RequestBaseConfigure maxRedirects(int maxRedirects);

    RequestBaseConfigure maxRetries(int maxRetries);

    RequestBaseConfigure readTimeout(long readTimeout);
}
