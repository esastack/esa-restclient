package esa.restclient.core.interceptor;


import esa.restclient.core.request.HttpRequest;
import esa.restclient.core.response.HttpResponse;

import java.util.concurrent.CompletionStage;

public interface Interceptor {
    CompletionStage<HttpResponse> proceed(HttpRequest request, InvokeChain next);
}
