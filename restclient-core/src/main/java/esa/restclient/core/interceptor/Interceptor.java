package esa.restclient.core.interceptor;


import esa.httpclient.core.util.Ordered;
import esa.restclient.core.exec.InvokeChain;
import esa.restclient.core.request.HttpRequest;
import esa.restclient.core.request.RestHttpRequest;
import esa.restclient.core.response.HttpResponse;
import esa.restclient.core.response.RestHttpResponse;

import java.util.concurrent.CompletionStage;

public interface Interceptor extends Ordered {
    CompletionStage<RestHttpResponse> proceed(RestHttpRequest request, InvokeChain next);
}
