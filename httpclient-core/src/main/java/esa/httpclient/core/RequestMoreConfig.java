package esa.httpclient.core;

import java.util.Map;

// TODO: 为何要单独拆分出来
public interface RequestMoreConfig {

    RequestMoreConfig addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    RequestMoreConfig addParams(Map<String, String> params);
}
