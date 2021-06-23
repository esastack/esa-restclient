package esa.restclient.core.exec;


import esa.restclient.core.request.RestHttpRequest;
import esa.restclient.core.response.RestHttpResponse;

import java.util.concurrent.CompletionStage;


public interface RestRequestExecutor {
    CompletionStage<RestHttpResponse> execute(RestHttpRequest request);
}
