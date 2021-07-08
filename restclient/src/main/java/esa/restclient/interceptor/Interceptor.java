package esa.restclient.interceptor;

import esa.httpclient.core.util.Ordered;
import esa.restclient.exec.InvocationChain;
import esa.restclient.RestRequest;
import esa.restclient.RestResponse;
import esa.restclient.exec.RequestAction;

import java.util.concurrent.CompletionStage;

public interface Interceptor extends Ordered {

    CompletionStage<RestResponse> proceed(RestRequest request, RequestAction requestAction, InvocationChain next);

}
