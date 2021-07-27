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

import esa.commons.StringUtils;
import esa.commons.logging.Logger;
import esa.httpclient.core.Scheme;
import esa.httpclient.core.exception.ClosedConnectionException;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;

final class Utils {

    static final ConnectException CONNECT_INACTIVE = new ConnectException("Connection inactive");
    static final ConnectException WRITE_BUF_IS_FULL = new ConnectException("Connection write buffer is full");

    private static final Logger logger = LoggerUtils.logger();

    static void handleH1ChannelEx(HandleRegistry registry,
                                  Channel channel,
                                  int reusableRequestId,
                                  Throwable cause,
                                  boolean enableLog) {
        final ResponseHandle handle = registry.remove(reusableRequestId);
        if (handle == null) {
            return;
        }

        boolean hasLogged = false;
        if (cause instanceof ClosedConnectionException) {
            if (logger.isDebugEnabled()) {
                logger.debug("ClosedConnectionException occurred in connection: {}",
                        channel, cause);
            }
            hasLogged = true;
        } else if (cause instanceof IOException) {
            if (logger.isDebugEnabled()) {
                logger.debug("IOException occurred in connection: {}", channel, cause);
            } else {
                logger.warn("Exception occurred in connection: {}," +
                        " maybe server has closed connection", channel);
            }
            hasLogged = true;
        }

        Utils.handleException(handle, cause, !hasLogged && enableLog);
    }

    static void handleH2ChannelEx(HandleRegistry registry,
                                  Channel channel,
                                  Throwable cause) {
        if (cause instanceof ClosedConnectionException) {
            if (logger.isDebugEnabled()) {
                logger.debug("ClosedConnectionException occurred in connection: {}",
                        channel, cause);
            }
        } else if (cause instanceof IOException) {
            if (logger.isDebugEnabled()) {
                logger.debug("IOException occurred in connection: {}", channel, cause);
            } else {
                logger.warn("Exception occurred in connection: {}," +
                        " maybe server has closed connection", channel);
            }
        }

        channel.closeFuture().addListener(future -> registry.handleAndClearAll((h) -> {
            try {
                Utils.handleException(h, cause, false);
            } catch (Throwable ex0) {
                // Ignore
            }
        }));
    }

    static boolean handleIdleEvt(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            final IdleStateEvent idleEvt = (IdleStateEvent) evt;
            if (IdleState.ALL_IDLE == idleEvt.state()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Close idle connection: {}", ctx.channel());
                }

                // use ctx.channel().close() to fire channelInactive event from the tail of pipeline instead of
                // ctx.close()
                ctx.channel().close();
                return true;
            }
        }

        return false;
    }

    static void runInChannel(Channel channel, Runnable runnable) {
        if (channel.eventLoop().inEventLoop()) {
            runnable.run();
        } else {
            channel.eventLoop().execute(runnable);
        }
    }

    static Http2Headers standardHeaders(Http2Headers headers) {
        for (Http2Headers.PseudoHeaderName name : Http2Headers.PseudoHeaderName.values()) {
            headers.remove(name.value());
        }

        return headers;
    }

    static boolean getValue(Boolean value, boolean defaultValue) {
        return value != null ? value : defaultValue;
    }

    static void handleException(ResponseHandle handle, Throwable cause, boolean enableLog) {
        // Maybe the handle has been removed by timeout checker or the request has ended normally.
        if (handle == null) {
            return;
        }

        if (enableLog) {
            logger.warn("Unexpected exception occurred, and the request has to end in error.", cause);
        }
        handle.onError(cause);
    }

    static void tryRelease(ReferenceCounted msg) {
        if (msg == null) {
            return;
        }
        if (msg.refCnt() > 0) {
            ReferenceCountUtil.safeRelease(msg);
        }
    }

    static Scheme toScheme(URI uri) {
        final String scheme = uri.getScheme();
        if (StringUtils.isEmpty(scheme)) {
            return Scheme.HTTP;
        }

        if (Scheme.HTTPS.name0().equalsIgnoreCase(scheme)) {
            return Scheme.HTTPS;
        }

        return Scheme.HTTP;
    }

}
