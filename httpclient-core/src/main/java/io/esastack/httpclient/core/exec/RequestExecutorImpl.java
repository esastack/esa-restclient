/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.httpclient.core.exec;

import esa.commons.Checks;
import esa.commons.annotation.Internal;
import esa.commons.collection.AttributeKey;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.Listener;

import java.util.concurrent.CompletableFuture;

@Internal
public class RequestExecutorImpl implements RequestExecutor {

    static final AttributeKey<Listener> LISTENER_KEY = AttributeKey.valueOf("$listener");

    private final Interceptor[] interceptors;
    private final HttpTransceiver transceiver;

    public RequestExecutorImpl(Interceptor[] interceptors,
                               HttpTransceiver transceiver) {
        Checks.checkNotNull(interceptors, "interceptors");
        Checks.checkNotNull(transceiver, "transceiver");
        this.transceiver = transceiver;
        this.interceptors = interceptors;
    }

    @Override
    public CompletableFuture<HttpResponse> execute(HttpRequest request, ExecContext execContext) {
        ExecChain chain = LinkedExecChain.from(interceptors, transceiver, execContext);

        execContext.listener().onInterceptorsStart(request, chain.ctx());
        chain.ctx().attrs().attr(LISTENER_KEY).set(execContext.listener());
        return chain.proceed(request);
    }
}
