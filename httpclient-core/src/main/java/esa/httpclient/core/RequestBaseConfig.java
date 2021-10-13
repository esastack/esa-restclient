package esa.httpclient.core;

public interface RequestBaseConfig {

    RequestBaseConfig enableUriEncode();

    RequestBaseConfig disableExpectContinue();

    RequestBaseConfig maxRedirects(int maxRedirects);

    RequestBaseConfig maxRetries(int maxRetries);

    RequestBaseConfig readTimeout(long readTimeout);
}
