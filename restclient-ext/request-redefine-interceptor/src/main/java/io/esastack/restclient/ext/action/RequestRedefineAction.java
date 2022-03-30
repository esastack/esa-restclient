package io.esastack.restclient.ext.action;

import io.esastack.restclient.RestResponse;
import io.esastack.restclient.ext.RedefineContext;

import java.util.concurrent.CompletionStage;

public interface RequestRedefineAction {

    CompletionStage<RestResponse> doAction(RedefineContext context);
}
