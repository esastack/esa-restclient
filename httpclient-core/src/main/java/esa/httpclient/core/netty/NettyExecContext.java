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
import esa.httpclient.core.Handle;
import esa.httpclient.core.Handler;
import esa.httpclient.core.Listener;
import esa.httpclient.core.exec.ExecContext;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NettyExecContext extends ExecContext {

    private volatile CompletableFuture<SegmentWriter> segmentWriter;

    public NettyExecContext(Context ctx, Listener listener, Consumer<Handle> handle, Handler handler) {
        super(ctx, listener, handle, handler);
    }

    void segmentWriter(CompletableFuture<SegmentWriter> segmentWriter) {
        this.segmentWriter = segmentWriter;
    }

    Optional<CompletableFuture<SegmentWriter>> segmentWriter() {
        return Optional.ofNullable(segmentWriter);
    }

}

