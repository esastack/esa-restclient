package esa.restclient.exec;

import esa.restclient.RestRequest;
import esa.restclient.RestResponseBase;

import java.util.concurrent.CompletionStage;

public interface RestRequestExecutor {

    CompletionStage<RestResponseBase> execute(RestRequest request);

}
