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

import esa.commons.Checks;
import io.esastack.httpclient.core.config.SslOptions;
import io.esastack.httpclient.core.spi.SslEngineFactory;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.util.ReferenceCountUtil;

import javax.net.ssl.SSLEngine;

class SslEngineFactoryImpl implements SslEngineFactory {

    private final SslContext sslContext;

    SslEngineFactoryImpl(SslContext sslContext) {
        Checks.checkNotNull(sslContext, "sslContext");
        this.sslContext = sslContext;
    }

    @Override
    public SSLEngine create(SslOptions options, String peerHost, int peerPort) {
        return sslContext.newEngine(ByteBufAllocator.DEFAULT, peerHost, peerPort);
    }

    @Override
    public void onDestroy() {
        ReferenceCountUtil.safeRelease(sslContext);
    }
}
