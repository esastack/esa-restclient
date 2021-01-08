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

import esa.httpclient.core.Context;
import esa.httpclient.core.HttpRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;

/**
 * The writer can convert, write and flush given {@link HttpRequest} and {@link HttpHeaders} to the
 * specified {@link Channel}.
 */
public interface RequestWriter<Request extends HttpRequest> {

    /**
     * Convert, write and flush the given request to the specified channel.
     *
     * @param request request
     * @param channel channel
     * @param ctx     ctx
     * @param uriEncodeEnabled enabled uriEncodeEnabled or not
     * @param version version
     * @param http2   http2 or not
     * @return future
     * @throws IOException ex
     */
    ChannelFuture writeAndFlush(Request request,
                                Channel channel,
                                Context ctx,
                                boolean uriEncodeEnabled,
                                HttpVersion version,
                                boolean http2) throws IOException;
}
