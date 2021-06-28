package esa.restclient.interceptor;


import esa.httpclient.core.util.Ordered;
import esa.restclient.exec.InvokeChain;
import esa.restclient.request.RestHttpRequest;
import esa.restclient.response.RestHttpResponse;

import java.util.concurrent.CompletionStage;

public interface Interceptor extends Ordered {
    CompletionStage<RestHttpResponse> proceed(RestHttpRequest request, InvokeChain next);
}