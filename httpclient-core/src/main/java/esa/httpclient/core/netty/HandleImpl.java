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
import esa.commons.http.HttpVersion;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.Handle;
import esa.httpclient.core.Handler;

import java.util.function.Consumer;

public class HandleImpl implements Handle {

    final NettyResponse underlying;

    protected volatile Consumer<Buffer> data;
    protected volatile Consumer<HttpHeaders> trailers;
    protected volatile Consumer<Void> end;
    protected volatile Consumer<Throwable> error;

    volatile Consumer<Void> start;

    public HandleImpl(NettyResponse underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
    }

    public HandleImpl(NettyResponse underlying, Handler handler) {
        Checks.checkNotNull(underlying, "underlying");
        Checks.checkNotNull(handler, "handler");
        this.underlying = underlying;
        this.start = (v) -> handler.onStart();
        this.data = handler::onData;
        this.trailers = handler::onTrailers;
        this.end = (v) -> handler.onEnd();
        this.error = handler::onError;
    }

    public HandleImpl(NettyResponse underlying, Consumer<Handle> handle0) {
        Checks.checkNotNull(underlying, "underlying");
        Checks.checkNotNull(handle0, "handle0");
        this.underlying = underlying;
        handle0.accept(this);
    }

    @Override
    public Handle onStart(Consumer<Void> h) {
        this.start = h;
        return this;
    }

    @Override
    public Handle onData(Consumer<Buffer> h) {
        this.data = h;
        return this;
    }

    @Override
    public Handle onTrailer(Consumer<HttpHeaders> h) {
        this.trailers = h;
        return this;
    }

    @Override
    public Handle onEnd(Consumer<Void> h) {
        this.end = h;
        return this;
    }

    @Override
    public Handle onError(Consumer<Throwable> h) {
        this.error = h;
        return this;
    }

    @Override
    public Buffer body() {
        return underlying.body();
    }

    @Override
    public HttpHeaders trailers() {
        return underlying.trailers();
    }

    @Override
    public int status() {
        return underlying.status();
    }

    @Override
    public HttpVersion version() {
        return underlying.version();
    }

    @Override
    public HttpHeaders headers() {
        return underlying.headers();
    }

    @Override
    public boolean aggregated() {
        return underlying.aggregated();
    }
}
