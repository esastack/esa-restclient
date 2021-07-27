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
package esa.httpclient.core.netty;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.HttpMessage;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.exec.ExecContext;
import esa.httpclient.core.util.LoggerUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

class ResponseHandle {

    final HandleImpl handle;
    final HttpRequest request;

    private final AtomicBoolean ended = new AtomicBoolean();
    private final ExecContext execCtx;
    private final TimeoutHandle tHandle;
    private final CompletableFuture<HttpResponse> response;

    long remaining = -1L;

    ResponseHandle(HandleImpl handle,
                   HttpRequest request,
                   ExecContext execCtx,
                   TimeoutHandle tHandle,
                   CompletableFuture<HttpResponse> response) {
        Checks.checkNotNull(handle, "handle");
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(execCtx, "execCtx");
        Checks.checkNotNull(response, "response");
        Checks.checkNotNull(tHandle, "tHandle");
        this.handle = handle;
        this.tHandle = tHandle;
        this.request = request;
        this.execCtx = execCtx;
        this.response = response;
    }

    public void onMessage(HttpMessage message) {
        if (ended.get()) {
            return;
        }

        try {
            handle.underlying.message(message);
            tHandle.onMessageReceived(request, execCtx.ctx(), message);
            if (handle.start != null) {
                handle.start.accept(null);
            }
        } catch (Throwable ex) {
            onError(ex);
        }
    }

    public void onData(Buffer content) {
        if (ended.get()) {
            return;
        }

        try {
            if (handle.data != null) {
                handle.data.accept(content);
            }
        } catch (Throwable ex) {
            onError(ex);
        }
    }

    public void onEnd() {
        if (ended.get()) {
            return;
        }

        try {
            if (ended.compareAndSet(false, true)) {
                if (handle.end != null) {
                    handle.end.accept(null);
                }
                response.complete(handle.underlying);
                tHandle.onCompleted(request, execCtx.ctx(), handle.underlying);
            }
        } catch (Throwable ex) {
            // Reset the ended flag so that onError can have chance to execute.
            onError0(ex);
        }
    }

    public void onError(Throwable cause) {
        if (ended.compareAndSet(false, true)) {
            onError0(cause);
        }
    }

    public void onTrailers(HttpHeaders trailers) {
        if (ended.get()) {
            return;
        }

        try {
            if (handle.trailers != null) {
                handle.trailers.accept(trailers);
            }
        } catch (Throwable ex) {
            onError(ex);
        }
    }

    public ExecContext ctx() {
        return execCtx;
    }

    private void onError0(Throwable cause) {
        response.completeExceptionally(cause);
        try {
            if (handle.error != null) {
                handle.error.accept(cause);
            }

            tHandle.onError(request, execCtx.ctx(), cause);
        } catch (Throwable ex) {
            LoggerUtils.logger().error("Unexpected exception occurred on handle#onError0", cause);
        }
    }
}
