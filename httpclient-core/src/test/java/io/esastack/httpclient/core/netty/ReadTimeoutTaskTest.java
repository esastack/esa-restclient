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
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.Listener;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.EventLoop;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadTimeoutTaskTest {

    @SuppressWarnings("unchecked")
    @Test
    void testRun() throws InterruptedException {
        final int requestId1 = 1;
        final AtomicInteger closed = new AtomicInteger(0);
        final AtomicInteger error = new AtomicInteger(0);
        final EmbeddedChannel embeddedChannel = new EmbeddedChannel();

        final DelegatingChannel channel = new DelegatingChannel(embeddedChannel, closed);

        final HandleRegistry registry = mock(HandleRegistry.class);

        final Handler0 adapter0 = new Handler0(mock(HttpRequest.class),
                mock(Context.class),
                mock(Listener.class),
                mock(CompletableFuture.class),
                error);

        when(registry.remove(1)).thenReturn(adapter0);

        final ReadTimeoutTask task1 = new ReadTimeoutTask(requestId1,
                "",
                channel,
                registry);

        final CountDownLatch latch = new CountDownLatch(1);
        channel.closeFuture().addListener(future -> latch.countDown());
        task1.run(null);

        // Fire task
        embeddedChannel.runPendingTasks();

        // Wait unit finish closing
        latch.await(50, TimeUnit.MILLISECONDS);

        then(closed.intValue()).isEqualTo(1);
        then(error.intValue()).isEqualTo(1);
    }

    private static final class Handler0 extends NettyHandle {

        private final AtomicInteger error;

        private Handler0(HttpRequest request,
                         Context ctx,
                         Listener listener,
                         CompletableFuture<HttpResponse> response,
                         AtomicInteger error) {
            super(new DefaultHandle(ByteBufAllocator.DEFAULT), request, ctx, listener, response);
            this.error = error;
        }

        @Override
        public void onError(Throwable cause) {
            error.incrementAndGet();
            super.onError(cause);
        }
    }

    private static final class DelegatingChannel extends AbstractChannel {

        private final AtomicInteger closed;
        private final Channel delegate;

        private DelegatingChannel(Channel delegate, AtomicInteger closed) {
            super(null);
            this.delegate = delegate;
            this.closed = closed;
        }

        @Override
        public ChannelFuture close() {
            closed.incrementAndGet();
            return delegate.close();
        }

        @Override
        public EventLoop eventLoop() {
            return delegate.eventLoop();
        }

        @Override
        protected AbstractUnsafe newUnsafe() {
            return null;
        }

        @Override
        protected boolean isCompatible(EventLoop loop) {
            return false;
        }

        @Override
        protected SocketAddress localAddress0() {
            return null;
        }

        @Override
        protected SocketAddress remoteAddress0() {
            return null;
        }

        @Override
        protected void doBind(SocketAddress localAddress) {

        }

        @Override
        protected void doDisconnect() {

        }

        @Override
        protected void doClose() {

        }

        @Override
        protected void doBeginRead() {

        }

        @Override
        protected void doWrite(ChannelOutboundBuffer in) {

        }

        @Override
        public ChannelConfig config() {
            return null;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public ChannelMetadata metadata() {
            return null;
        }
    }
}
