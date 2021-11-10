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

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.netty.buffer.BufferImpl;
import io.esastack.httpclient.core.Handle;
import io.esastack.httpclient.core.util.BufferUtils;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

import java.util.function.Consumer;

import static io.esastack.httpclient.core.netty.Utils.tryRelease;

class DefaultHandle extends HandleImpl {

    private static final int MAX_COMPOSITE_BUFFER_COMPONENTS = 1024;

    private CompositeByteBuf body;

    DefaultHandle(ByteBufAllocator alloc) {
        super(new NettyResponse(true));

        this.data = (d) -> {
            if (d.readableBytes() > 0) {
                if (body == null) {
                    body = alloc.compositeBuffer(MAX_COMPOSITE_BUFFER_COMPONENTS);
                }
                body.addComponent(true, BufferUtils.toByteBuf(d).retain());
            }
        };

        this.end = (v) -> {
            if (body == null) {
                super.underlying.body(BufferUtil.empty());
            } else {
                super.underlying.body(new BufferImpl(Unpooled.copiedBuffer(body)));
            }

            // Try to release the staged body.
            tryRelease(body);
        };

        this.trailers = (trailers) -> trailers().add(trailers);

        // Try to release the staged body.
        this.error = (th) -> tryRelease(body);
    }

    @Override
    public Handle onStart(Consumer<Void> h) {
        // Do nothing
        return this;
    }

    @Override
    public DefaultHandle onData(Consumer<Buffer> h) {
        // Do nothing
        return this;
    }

    @Override
    public DefaultHandle onTrailer(Consumer<HttpHeaders> h) {
        // Do nothing
        return this;
    }

    @Override
    public DefaultHandle onEnd(Consumer<Void> h) {
        // Do nothing
        return this;
    }

    @Override
    public DefaultHandle onError(Consumer<Throwable> h) {
        // Do nothing
        return this;
    }
}
