package esa.httpclient.core;

import java.util.Map;

public interface RequestConfig {

    RequestConfig enableUriEncode();

    RequestConfig disableExpectContinue();

    RequestConfig maxRedirects(int maxRedirects);

    RequestConfig maxRetries(int maxRetries);

    RequestConfig readTimeout(int readTimeout);

    RequestConfig addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    RequestConfig addParams(Map<String, String> params);
}
