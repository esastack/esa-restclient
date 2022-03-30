package io.esastack.restclient.ext;

import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;

import java.util.concurrent.CompletionStage;

public interface RedefineContext {
    RestRequest request();

    CompletionStage<RestResponse> next();
}
