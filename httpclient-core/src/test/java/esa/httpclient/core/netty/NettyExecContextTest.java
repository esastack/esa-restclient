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
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.Listener;
import esa.httpclient.core.NoopListener;
import esa.httpclient.core.exec.ExecContext;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class NettyExecContextTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new NettyExecContext(null, NoopListener.INSTANCE,
                null, null));
        assertThrows(NullPointerException.class, () -> new NettyExecContext(new Context(), null,
                null, null));

        assertDoesNotThrow(() -> new NettyExecContext(new Context(), NoopListener.INSTANCE,
                null, null));

        final Context ctx = new Context();
        final Listener listener = NoopListener.INSTANCE;
        final Consumer<Handle> handle = (h) -> {};
        final Handler handler = mock(Handler.class);

        final NettyExecContext execCtx = new NettyExecContext(ctx, listener, handle, handler);
        assertSame(ctx, execCtx.ctx());
        assertSame(listener, execCtx.listener());

        assertNotNull(execCtx.handleImpl(mock(HttpRequest.class)));
        assertNotNull(new ExecContext(new Context(), NoopListener.INSTANCE,
                handle, null).handleImpl(mock(HttpRequest.class)));
        assertNotNull(new ExecContext(new Context(), NoopListener.INSTANCE,
                null, handler).handleImpl(mock(HttpRequest.class)));

        assertNull(new ExecContext(new Context(), NoopListener.INSTANCE,
                null, null).handleImpl(mock(HttpRequest.class)));
    }

    @Test
    void testSetAndGet100ContinueCallback() {
        final NettyExecContext context = new NettyExecContext(new Context(), NoopListener.INSTANCE,
                null, null);
        final Runnable runnable = () -> {
        };

        context.set100ContinueCallback(runnable);
        then(context.remove100ContinueCallback()).isSameAs(runnable);
        then(context.remove100ContinueCallback()).isNull();
    }

    @Test
    void testGetterAndSetter() {
        final CompletableFuture<SegmentWriter> writer = new CompletableFuture<>();
        final NettyExecContext context = new NettyExecContext(new Context(), NoopListener.INSTANCE,
                null, null);
        context.segmentWriter(writer);
        then(context.segmentWriter().orElse(null)).isSameAs(writer);
        context.segmentWriter(null);
        then(context.segmentWriter().isPresent()).isFalse();
    }
}
