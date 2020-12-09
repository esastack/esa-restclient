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
import esa.httpclient.core.FileRequest;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.MultipartRequest;
import esa.httpclient.core.PlainRequest;
import esa.httpclient.core.RequestOptions;
import esa.httpclient.core.RequestType;
import io.netty.handler.codec.http.HttpHeaderValues;

import java.util.concurrent.CompletableFuture;

import static esa.httpclient.core.ContextNames.EXPECT_CONTINUE_ENABLED;

/**
 * This interceptor is designed to handle request which needs 100-continue negotiation with remote peer.
 * When the {@link RequestOptions#expectContinueEnabled()} is true, current interceptor will
 * take effect to the corresponding {@link HttpRequest}.
 */
public class ExpectContinueInterceptor implements Interceptor {

    @Override
    public CompletableFuture<HttpResponse> proceed(HttpRequest request, ExecChain next) {
        // Pass directly if not configured
        if (!Boolean.TRUE.equals(next.ctx().getAttr(EXPECT_CONTINUE_ENABLED))) {
            return next.proceed(request);
        }

        // Chunk request is not allowed to handle Expect: 100-Continue
        if (RequestType.CHUNK.equals(request.type())) {
            next.ctx().removeAttr(EXPECT_CONTINUE_ENABLED);
            return next.proceed(request);
        }

        if (emptyBody(request)) {
            next.ctx().removeAttr(EXPECT_CONTINUE_ENABLED);
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
        if (RequestType.PLAIN.equals(request.type())) {
            return ((PlainRequest) request).body() == null || ((PlainRequest) request).body().length == 0;
        } else if (RequestType.MULTIPART.equals(request.type())) {
            return ((MultipartRequest) request).files().isEmpty()
                    && ((MultipartRequest) request).attributes().isEmpty();
        } else if (RequestType.FILE.equals(request.type())) {
            return ((FileRequest) request).file() == null;
        }

        return true;
    }

}
