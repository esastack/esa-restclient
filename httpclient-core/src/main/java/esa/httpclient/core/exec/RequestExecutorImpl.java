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
import esa.httpclient.core.Context;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.RequestOptions;
import esa.httpclient.core.netty.HandleImpl;
import esa.httpclient.core.netty.NettyResponse;
import esa.httpclient.core.util.LoggerUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static esa.httpclient.core.ContextNames.EXPECT_CONTINUE_ENABLED;
import static esa.httpclient.core.ContextNames.MAX_REDIRECTS;
import static esa.httpclient.core.ContextNames.MAX_RETRIES;

public class RequestExecutorImpl implements RequestExecutor {

    static final String LISTENER_KEY = "$listener";

    private final HttpClientBuilder builder;
    private final Interceptor[] interceptors;
    private final HttpTransceiver transceiver;
    private final int defaultMaxRedirects;
    private final int defaultMaxRetries;
    private final boolean defaultExpectContinueEnabled;

    public RequestExecutorImpl(HttpClientBuilder builder,
                               Interceptor[] interceptors,
                               HttpTransceiver transceiver,
                               int defaultMaxRedirects,
                               int defaultMaxRetries,
                               boolean defaultExpectContinueEnabled) {
        Checks.checkNotNull(builder, "HttpClientBuilder must not be null");
        Checks.checkNotNull(interceptors, "Interceptors must not be null");
        Checks.checkNotNull(transceiver, "NettyTransceiver must not be null");
        this.builder = builder;
        this.transceiver = transceiver;
        this.interceptors = interceptors;
        this.defaultMaxRedirects = defaultMaxRedirects;
        this.defaultMaxRetries = defaultMaxRetries;
        this.defaultExpectContinueEnabled = defaultExpectContinueEnabled;
    }

    @Override
    public CompletableFuture<HttpResponse> execute(HttpRequest request,
                                                   Context ctx,
                                                   Listener listener) {
        ExecChain chain = build(request,
                (l, r) -> decideCustomHandle(request),
                ctx,
                listener,
                decideReadTimeout(request.config().readTimeout()));

        listener.onInterceptorsStart(request, chain.ctx());
        chain.ctx().setAttr(LISTENER_KEY, listener);
        return chain.proceed(request);
    }

    /**
     * Builds a execution chain to execute {@link HttpRequest}
     *
     * @param request      request
     * @param handle       handler
     * @param ctx          ctx
     * @param listener     listener
     * @param readTimeout  readTimeout
     * @return chain       execution chain
     */
    ExecChain build(HttpRequest request,
                    BiFunction<Listener, CompletableFuture<HttpResponse>, HandleImpl> handle,
                    Context ctx,
                    Listener listener,
                    int readTimeout) {
        final RequestOptions options = request.config();
        if (options.expectContinueEnabled() != null
                ? options.expectContinueEnabled() : defaultExpectContinueEnabled) {
            ctx.setAttr(EXPECT_CONTINUE_ENABLED, true);
        }

        ctx.setAttr(MAX_RETRIES, options.maxRetries() > 0
                ? options.maxRetries() : options.maxRetries() == 0
                ? 0 : defaultMaxRetries);

        ctx.setAttr(MAX_REDIRECTS, options.maxRedirects() > 0
                ? options.maxRedirects() : options.maxRedirects() == 0
                ? 0 : defaultMaxRedirects);

        return LinkedExecChain.from(interceptors, transceiver, handle, ctx, listener, readTimeout);
    }

    private HandleImpl decideCustomHandle(HttpRequest request) {
        final RequestOptions options = request.config();
        if (options.handler() != null && options.handle() != null) {
            LoggerUtils.logger().warn("Both handler and consumer<handle> are found to handle the" +
                    "inbound message, the handler will be used, uri: {}", request.uri());
        }
        if (options.handler() != null) {
            return new HandleImpl(new NettyResponse(false), options.handler());
        } else if (options.handle() != null) {
            return new HandleImpl(new NettyResponse(false), options.handle());
        }

        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger().debug("The default handle will be used to aggregate the inbound message to" +
                    " response");
        }

        return null;
    }

    private int decideReadTimeout(int readTimeout) {
        if (readTimeout > 0) {
            return readTimeout;
        }

        return builder.readTimeout();
    }

}
