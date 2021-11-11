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
package io.esastack.restclient.exec;

import io.esastack.restclient.RequestTransceiver;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponseBase;

import java.util.List;
import java.util.concurrent.CompletionStage;

public final class RestRequestExecutorImpl implements RestRequestExecutor {

    private final InvocationChain invocationChain;

    public RestRequestExecutorImpl(RestClientOptions clientOptions) {
        this.invocationChain = buildInvokeChain(clientOptions.unmodifiableInterceptors(),
                clientOptions);
    }

    @Override
    public CompletionStage<RestResponseBase> execute(RestRequest request) {
        return invocationChain.proceed(request)
                .thenApply(response -> (RestResponseBase) response);
    }

    private InvocationChain buildInvokeChain(List<ClientInterceptor> orderedInterceptors,
                                             RestClientOptions clientOptions) {
        InvocationChain invocationChain = new RequestTransceiver(clientOptions);
        int size = orderedInterceptors.size();
        if (size == 0) {
            return invocationChain;
        }
        for (int i = size - 1; i >= 0; i--) {
            invocationChain = new InvocationChainImpl(orderedInterceptors.get(i), invocationChain);
        }
        return invocationChain;
    }
}
