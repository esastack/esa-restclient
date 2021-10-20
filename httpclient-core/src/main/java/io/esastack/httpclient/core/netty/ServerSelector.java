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
package io.esastack.httpclient.core.netty;

import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpUri;
import io.esastack.httpclient.core.Scheme;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

interface ServerSelector {

    ServerSelector DEFAULT = (request, ctx) -> {
        final HttpUri uri = request.uri();
        int port = uri.port();
        if (port <= 0) {
            port = Scheme.HTTPS == Utils.toScheme(uri.netURI())
                    ? Scheme.HTTPS.port() : Scheme.HTTP.port();
        }

        return InetSocketAddress.createUnresolved(uri.host(), port);
    };

    /**
     * Detects target server to connect.
     *
     * @param request request
     * @param ctx     ctx
     * @return target server address
     */
    SocketAddress select(HttpRequest request, Context ctx);

}
