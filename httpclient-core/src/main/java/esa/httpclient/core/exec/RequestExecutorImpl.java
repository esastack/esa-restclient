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
package esa.httpclient.core.exec;

import esa.commons.Checks;
import esa.commons.annotation.Internal;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;

import java.util.concurrent.CompletableFuture;

@Internal
public class RequestExecutorImpl implements RequestExecutor {

    static final String LISTENER_KEY = "$listener";

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
        chain.ctx().setAttr(LISTENER_KEY, execContext.listener());
        return chain.proceed(request);
    }
}
