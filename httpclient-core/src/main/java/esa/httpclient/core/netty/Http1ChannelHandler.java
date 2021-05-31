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
import esa.commons.netty.core.BufferImpl;
import esa.commons.netty.http.Http1HeadersAdaptor;
import esa.httpclient.core.exception.ClosedConnectionException;
import esa.httpclient.core.exception.ContentOverSizedException;
import esa.httpclient.core.exception.ProtocolException;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;

import static esa.httpclient.core.netty.Utils.handleIdleEvt;

class Http1ChannelHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final HandleRegistry registry;
    private final long maxContentLength;
    private volatile ChannelHandlerContext ctx;

    private int reusableRequestId;
    private boolean continue100Received;
    private long remaining = -1L;

    Http1ChannelHandler(HandleRegistry registry, long maxContentLength) {
        Checks.checkNotNull(registry, "HandleRegistry must not be null");
        this.registry = registry;
        this.maxContentLength = maxContentLength;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.handlerAdded(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        try {
            onError(cause, true);
        } finally {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        onError(new ClosedConnectionException("Connection: " + ctx.channel() + " inactive"), false);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!handleIdleEvt(ctx, evt)) {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * We don't need to care the releasing of {@code msg}, more information at
     * {@link SimpleChannelInboundHandler#channelRead(ChannelHandlerContext, Object)}.
     *
     * @param ctx ctx
     * @param msg msg
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        final NettyHandle handle = registry.get(reusableRequestId);
        // Handle == null means the request has ended(timeout, exceeds maxContentLength, or others),
        // and the current msg should be ignored.
        if (handle == null) {
            if (LoggerUtils.logger().isDebugEnabled()) {
                int size = (msg instanceof HttpResponse) ? 0 : ((HttpContent) msg).content().readableBytes();
                LoggerUtils.logger().debug("There is no handler to handle inbound object, size: {}" +
                        " connection: {}", size, ctx.channel());
            }
            return;
        }

        if (!msg.decoderResult().isSuccess()) {
            handleDecodeError(msg.decoderResult().cause());
            return;
        }

        if (msg instanceof HttpResponse) {
            handleResponse(handle, (HttpResponse) msg);
        } else if (msg instanceof HttpContent) {
            if (continue100Received) {
                // Ignore all the contents follow closely to 100-expect-continue
                // until the last one arrived
                if (msg instanceof LastHttpContent) {
                    continue100Received = false;
                }
                return;
            }

            // Handle empty last content.
            if (LastHttpContent.EMPTY_LAST_CONTENT == msg) {
                handle.onEnd();
                registry.remove(reusableRequestId);
                return;
            }

            final ByteBuf content = ((HttpContent) msg).content();
            if (content.readableBytes() > 0) {
                ByteBuf readableBytes;
                boolean exceeded = false;

                long remainingTemp = remaining;
                if (remainingTemp == -1L) {
                    readableBytes = content;
                } else {
                    long total = content.readableBytes();
                    remainingTemp -= total;
                    if (remainingTemp >= 0L) {
                        readableBytes = content;
                    } else {
                        remainingTemp = 0L;
                        readableBytes = content.slice(0, (int) (remainingTemp + total));
                        exceeded = true;
                    }
                }

                handle.onData(new BufferImpl(readableBytes.duplicate()));
                if (exceeded) {
                    String errMsg = String.format("Content length exceeded %d bytes", maxContentLength);
                    onError(new ContentOverSizedException(errMsg), true);
                    return;
                }

                remaining = remainingTemp;
            }

            if (msg instanceof LastHttpContent) {
                HttpHeaders trailers = ((LastHttpContent) msg).trailingHeaders();
                if (!trailers.isEmpty()) {
                    handle.onTrailers(new Http1HeadersAdaptor(trailers));
                }
                handle.onEnd();
                registry.remove(reusableRequestId);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void onError(Throwable cause, boolean enableLog) {
        Utils.handleH1ChannelEx(registry, ctx.channel(), reusableRequestId, cause, enableLog);
    }

    void updateRequestId(int requestId) {
        Utils.runInChannel(ctx.channel(), () -> {
            this.reusableRequestId = requestId;
            this.continue100Received = false;
            this.remaining = -1L;
        });
    }

    HandleRegistry getRegistry() {
        return registry;
    }

    private void handleDecodeError(Throwable cause) {
        final String errMsg = "Failed to decode response, connection: " + ctx.channel();
        onError(new ProtocolException(errMsg, cause), true);
    }

    private void handleResponse(NettyHandle handle, HttpResponse msg) {
        // Handle Except: 100-continue' preface response
        // Note: a request which has a 'Expect: 100-continue' will receive two full http response
        // and only the last one is the common response to the request
        final int status = msg.status().code();
        if (status == HttpResponseStatus.CONTINUE.code()) {
            continue100Received = true;
            final Runnable runnable = ((NettyContext) handle.ctx()).remove100ContinueCallback();
            if (runnable != null) {
                runnable.run();
            }
            return;
        }

        // If maxContentLength != -1, validate contentLength firstly.
        if (isContentLengthInvalid(msg)) {
            String errMsg = String.format("Content length exceeded %d bytes", maxContentLength);
            onError(new ContentOverSizedException(errMsg), true);
            return;
        }

        handle.onMessage(HttpMessageImpl.from(msg));
    }

    /**
     * Tries to check content-length only when {@code maxContentLength} is configured.
     *
     * @param response      response
     * @return              true or false
     */
    private boolean isContentLengthInvalid(HttpMessage response) {
        if (maxContentLength > 0L) {
            long contentLength = -1L;
            try {
                String value = response.headers().get(HttpHeaderNames.CONTENT_LENGTH);
                if (value != null) {
                    contentLength = Long.parseLong(value);
                }
            } catch (NumberFormatException ex) {
                // Ignore
            }

            if (contentLength > 0L) {
                return contentLength > maxContentLength;
            } else {
                remaining = maxContentLength;
                return false;
            }
        }

        return false;
    }

}
