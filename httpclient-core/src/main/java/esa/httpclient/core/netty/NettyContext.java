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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NettyContext extends Context {

    private volatile Runnable continueCallback;
    protected volatile CompletableFuture<ChunkWriter> writer;

    void set100ContinueCallback(Runnable callback) {
        this.continueCallback = callback;
    }

    Runnable remove100ContinueCallback() {
        final Runnable callback0 = continueCallback;
        this.continueCallback = null;
        return callback0;
    }

    void setWriter(CompletableFuture<ChunkWriter> writer) {
        this.writer = writer;
    }

    Optional<CompletableFuture<ChunkWriter>> getWriter() {
        return Optional.ofNullable(writer);
    }

}

