package esa.httpclient.core;

public interface RequestBaseConfig {

    RequestMoreConfig enableUriEncode();

    RequestMoreConfig disableExpectContinue();

    RequestMoreConfig maxRedirects(int maxRedirects);

    RequestMoreConfig maxRetries(int maxRetries);

    RequestMoreConfig readTimeout(long readTimeout);
}
