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
package io.esastack.httpclient.core.netty;

import io.esastack.httpclient.core.HttpClientBuilder;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.config.ChannelPoolOptions;
import io.esastack.httpclient.core.exec.ExecContext;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

class NettyTransceiver extends HttpTransceiverImpl {

    NettyTransceiver(EventLoopGroup ioThreads, CachedChannelPools channelPools,
                     HttpClientBuilder builder, ChannelPoolOptions channelPoolOptions,
                     ChannelPoolFactory channelPoolFactory) {
        super(ioThreads, channelPools, builder, channelPoolOptions, channelPoolFactory);
    }

    @Override
    public CompletableFuture<HttpResponse> handle(HttpRequest request, ExecContext execCtx) {
        if (request.isSegmented() && execCtx instanceof NettyExecContext) {
            ((NettyExecContext) execCtx).segmentWriter(new CompletableFuture<>());
        }

        return super.handle(request, execCtx);
    }

    @Override
    protected void onAcquireChannelPoolFailure(HttpRequest request, ExecContext execCtx,
                                               SocketAddress address, Throwable cause) {
        super.onAcquireChannelPoolFailure(request, execCtx, address, cause);
        tryCompleteSegmentExceptionally(request, execCtx, cause);
    }

    @Override
    protected void onAcquireChannelFailure(HttpRequest request, SocketAddress address,
                                           ExecContext execCtx, CompletableFuture<HttpResponse> response,
                                           Throwable ex) {
        super.onAcquireChannelFailure(request, address, execCtx, response, ex);
        tryCompleteSegmentExceptionally(request, execCtx, ex);
    }

    @Override
    protected void completeExceptionally(HttpRequest request, ExecContext execCtx,
                                         CompletableFuture<HttpResponse> response, Throwable cause) {
        super.completeExceptionally(request, execCtx, response, cause);
        tryCompleteSegmentExceptionally(request, execCtx, cause);
    }

    @Override
    protected void afterWriting(int requestId, HttpRequest request,
                                ExecContext execCtx, RequestWriter writer,
                                ChannelFuture headFuture, ChannelFuture endFuture,
                                TimeoutHandle handle, HandleRegistry registry,
                                CompletableFuture<HttpResponse> response) {
        super.afterWriting(requestId, request, execCtx, writer, headFuture, endFuture,
                handle, registry, response);
        if (request.isSegmented() && execCtx instanceof NettyExecContext) {
            ((NettyExecContext) execCtx).segmentWriter().ifPresent(writer0 -> writer0.complete((SegmentWriter) writer));
        }
    }

    @Override
    protected void tryToCleanAndEndExceptionally(HttpRequest request, ExecContext execCtx,
                                                 int requestId, HandleRegistry registry,
                                                 TimeoutHandle handle, CompletableFuture<HttpResponse> response,
                                                 Throwable cause) {
        super.tryToCleanAndEndExceptionally(request, execCtx, requestId, registry, handle, response, cause);
        tryCompleteSegmentExceptionally(request, execCtx, cause);
    }

    private void tryCompleteSegmentExceptionally(HttpRequest request, ExecContext execCtx, Throwable cause) {
        if (request.isSegmented() && execCtx instanceof NettyExecContext) {
            ((NettyExecContext) execCtx).segmentWriter().ifPresent(writer -> writer.completeExceptionally(cause));
        }
    }
}

