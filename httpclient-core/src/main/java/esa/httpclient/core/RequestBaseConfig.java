package esa.httpclient.core;

import java.util.Map;

public interface RequestBaseConfig {

    RequestBaseConfig enableUriEncode();

    RequestBaseConfig disableExpectContinue();

    RequestBaseConfig maxRedirects(int maxRedirects);

    RequestBaseConfig maxRetries(int maxRetries);

    RequestBaseConfig readTimeout(int readTimeout);

    RequestBaseConfig addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    RequestBaseConfig addParams(Map<String, String> params);
}
