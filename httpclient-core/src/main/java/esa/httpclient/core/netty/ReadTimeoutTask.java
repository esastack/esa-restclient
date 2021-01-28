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

import esa.httpclient.core.util.LoggerUtils;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.net.SocketTimeoutException;

final class ReadTimeoutTask implements TimerTask {

    private final int requestId;
    private final String uri;
    private final Channel channel;
    private final HandleRegistry registry;

    ReadTimeoutTask(int requestId,
                    String uri,
                    Channel channel,
                    HandleRegistry registry) {
        this.requestId = requestId;
        this.uri = uri;
        this.channel = channel;
        this.registry = registry;
    }

    @Override
    public void run(Timeout timeout) {
        final NettyHandle handle = registry.remove(requestId);
        if (handle != null) {
            channel.eventLoop().execute(() -> handle.onError(new
                    SocketTimeoutException("Request: " + uri + " reads timeout")));
            channel.close();
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger().debug("Request: " + uri + " reads timeout, begin to close connection: "
                        + channel);
            }
        }
    }

    void cancel() {
        NettyHandle handle = registry.remove(requestId);
        if (handle != null) {
            handle.onError(new IllegalStateException("Request: " + uri +
                    " haven't finished while connection: " + channel + "has closed"));
        }
    }

}
