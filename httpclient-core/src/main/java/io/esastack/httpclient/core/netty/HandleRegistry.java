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

import io.esastack.httpclient.core.HttpRequest;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;

import java.util.function.Consumer;

/**
 * This class holds a {@link #handlers} which saves the mapper from {@link HttpRequest} to corresponding
 * {@link NettyHandle} by request's {@code requestId}.
 */
class HandleRegistry {

    private final IntObjectMap<NettyHandle> handlers = new IntObjectHashMap<>(16);

    private final int delta;
    private int requestId;

    HandleRegistry(int delta, int requestId) {
        this.requestId = requestId;
        this.delta = delta;
    }

    public synchronized NettyHandle remove(int requestId) {
        return handlers.remove(requestId);
    }

    public synchronized int put(NettyHandle handle) {
        this.requestId += delta;
        handlers.putIfAbsent(requestId, handle);
        return requestId;
    }

    public synchronized NettyHandle get(int requestId) {
        return handlers.get(requestId);
    }

    synchronized void handleAndClearAll(Consumer<NettyHandle> handle) {
        handlers.forEach((id, h) -> handle.accept(h));
        handlers.clear();
    }
}
