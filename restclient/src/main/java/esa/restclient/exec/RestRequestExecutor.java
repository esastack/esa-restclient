package esa.restclient.exec;

import esa.restclient.request.RestHttpRequest;
import esa.restclient.response.RestHttpResponse;

import java.util.concurrent.CompletionStage;

public interface RestRequestExecutor {

    CompletionStage<RestHttpResponse> execute(RestHttpRequest request);

}
