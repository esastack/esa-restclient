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
import esa.httpclient.core.Scheme;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;

import java.net.URI;

final class Utils {

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

    static void handleException(NettyHandle handle, Throwable cause, boolean enableLog) {
        // Maybe the handle has been removed by timeout checker or the request has ended normally.
        if (handle == null) {
            return;
        }

        if (enableLog) {
            LoggerUtils.logger().warn("Unexpected exception occurred, and request will end abnormally", cause);
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
