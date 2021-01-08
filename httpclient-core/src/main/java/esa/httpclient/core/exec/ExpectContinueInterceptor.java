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

import esa.commons.http.HttpHeaderNames;
import esa.httpclient.core.Context;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.handler.codec.http.HttpHeaderValues;

import java.util.concurrent.CompletableFuture;

/**
 * This interceptor is designed to handle request which needs 100-continue negotiation with remote peer.
 * When the {@link Context#expectContinueEnabled()} is true, current interceptor will
 * take effect to the corresponding {@link HttpRequest}.
 */
public class ExpectContinueInterceptor implements Interceptor {

    @Override
    public CompletableFuture<HttpResponse> proceed(HttpRequest request, ExecChain next) {
        // Pass directly if not configured
        if (!Boolean.TRUE.equals(next.ctx().expectContinueEnabled())) {
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger().debug("Pass ExpectContinue-interceptor directly, uri: {}",
                        request.uri().toString());
            }
            return next.proceed(request);
        }

        // Chunk request is not allowed to handle Expect: 100-Continue
        if (request.isSegmented()) {
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger().debug("Pass ExpectContinue-interceptor directly, uri: {}",
                        request.uri().toString());
            }
            return next.proceed(request);
        }

        if (emptyBody(request)) {
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger().debug("Pass ExpectContinue-interceptor directly due to empty body, uri: {}",
                        request.uri().toString());
            }
            return next.proceed(request);
        }

        if (!request.headers().contains(HttpHeaderNames.EXPECT)) {
            request.addHeader(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE);
        }

        return next.proceed(request);
    }

    @Override
    public int getOrder() {
        return -5000;
    }

    protected boolean emptyBody(HttpRequest request) {
        // NOTE: Expect-continue isn't supported for ChunkRequest
        if (request.isSegmented()) {
            return true;
        }

        if (request.isMultipart()) {
            return request.files().isEmpty() && request.attributes().isEmpty();
        }
        return request.file() == null
                && (request.bytes() == null || request.bytes().length == 0);
    }

}
