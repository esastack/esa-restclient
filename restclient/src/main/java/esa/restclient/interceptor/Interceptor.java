package esa.restclient.interceptor;

import esa.httpclient.core.util.Ordered;
import esa.restclient.exec.InvocationChain;
import esa.restclient.RestHttpRequest;
import esa.restclient.RestHttpResponse;

import java.util.concurrent.CompletionStage;

public interface Interceptor extends Ordered {

    CompletionStage<RestHttpResponse> proceed(RestHttpRequest request, InvocationChain next);

}
