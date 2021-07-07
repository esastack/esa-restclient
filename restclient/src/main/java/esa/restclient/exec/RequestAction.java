package esa.restclient.exec;

import esa.restclient.RestHttpRequest;
import esa.restclient.RestHttpResponse;

import java.util.concurrent.CompletionStage;

public interface RequestAction {
    CompletionStage<RestHttpResponse> doRequest(RestHttpRequest restHttpRequest);
}
