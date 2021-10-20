package io.esastack.restclient.exec;

import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponseBase;

import java.util.concurrent.CompletionStage;

public interface RestRequestExecutor {

    CompletionStage<RestResponseBase> execute(RestRequest request);

}
