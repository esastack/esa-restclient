package esa.restclient.exec;

import esa.restclient.RestRequest;
import esa.restclient.RestResponse;

import java.util.concurrent.CompletionStage;

public interface RequestAction {
    CompletionStage<RestResponse> doRequest(RestRequest restRequest);
}
