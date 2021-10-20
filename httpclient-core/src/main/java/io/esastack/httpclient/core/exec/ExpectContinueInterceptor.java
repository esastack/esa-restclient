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

import esa.commons.http.HttpHeaderNames;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.util.LoggerUtils;
import io.netty.handler.codec.http.HttpHeaderValues;

import java.util.concurrent.CompletableFuture;

/**
 * The interceptor designed to handle {@link HttpRequest} which needs 100-continue negotiation with remote peer.
 * When the {@link Context#isUseExpectContinue()} returns {@code true}, current interceptor will
 * take effect to the corresponding {@link HttpRequest}.
 */
public class ExpectContinueInterceptor implements Interceptor {

    @Override
    public CompletableFuture<HttpResponse> proceed(HttpRequest request, ExecChain next) {
        if (!Boolean.TRUE.equals(next.ctx().isUseExpectContinue())) {
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger().debug("100-Continue is disabled, uri: {}",
                        request.uri().toString());
            }
            return next.proceed(request);
        }

        if (request.isSegmented()) {
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger().debug("100-Continue is unsupported for segment request, uri: {}",
                        request.uri().toString());
            }
            request.headers().remove(HttpHeaderNames.EXPECT);
            return next.proceed(request);
        }

        if (emptyBody(request)) {
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger().debug("100-Continue is ignored due to empty body, uri: {}",
                        request.uri().toString());
            }
            request.headers().remove(HttpHeaderNames.EXPECT);
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
        // NOTE: Expect-continue isn't supported for SegmentRequest
        if (request.isSegmented()) {
            return true;
        }

        if (request.isMultipart()) {
            return request.files().isEmpty() && request.attrs().isEmpty();
        }
        return request.file() == null
                && (request.buffer() == null || !request.buffer().isReadable());
    }

}
