package esa.restclient.exec;

import esa.httpclient.core.util.Ordered;
import esa.restclient.RestRequest;
import esa.restclient.RestResponse;

import java.util.concurrent.CompletionStage;

public interface Interceptor extends Ordered {

    CompletionStage<RestResponse> proceed(RestRequest request, InvocationChain next);

}
