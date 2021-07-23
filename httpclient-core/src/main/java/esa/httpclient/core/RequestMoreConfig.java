package esa.httpclient.core;

import java.util.Map;

public interface RequestMoreConfig {

    RequestMoreConfig addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    RequestMoreConfig addParams(Map<String, String> params);
}
