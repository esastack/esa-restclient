package esa.restclient.exec;

import esa.restclient.RestHttpRequest;
import esa.restclient.RestHttpResponse;

import java.util.concurrent.CompletionStage;

public interface RestRequestExecutor {

    CompletionStage<RestHttpResponse> execute(RestHttpRequest request, RequestAction requestAction);

}
