/*
 * Copyright 2021 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restclient;

import io.esastack.httpclient.core.HttpResponse;
import io.esastack.restclient.exec.InvocationChain;

import java.util.concurrent.CompletionStage;

public final class RequestTransceiver implements InvocationChain {

    public RequestTransceiver() {
    }

    @Override
    public CompletionStage<RestResponse> proceed(RestRequest request) {
        if (!(request instanceof AbstractExecutableRestRequest)) {
            throw new IllegalStateException("The type(" + request.getClass()
                    + ") of the request is not AbstractExecutableRestRequest!" +
                    "The host of request : " + request.uri().host());
        }

        final AbstractExecutableRestRequest executableRequest = (AbstractExecutableRestRequest) request;

        return executableRequest.sendRequest()
                .thenApply((response) -> processResponse(executableRequest, response, executableRequest.clientOptions));
    }

    private RestResponse processResponse(RestRequestBase request,
                                         HttpResponse response,
                                         RestClientOptions clientOptions) {
        return new RestResponseBaseImpl(request, response, clientOptions);
    }
}
