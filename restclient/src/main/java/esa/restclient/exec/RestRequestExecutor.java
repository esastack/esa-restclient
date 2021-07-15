package esa.restclient.exec;

import esa.restclient.RestRequest;
import esa.restclient.RestResponse;

import java.util.concurrent.CompletionStage;

public interface RestRequestExecutor {

    CompletionStage<RestResponse> execute(RestRequest request);

}
