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

import io.esastack.commons.net.http.HttpHeaderValues;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.Listener;
import io.esastack.httpclient.core.ListenerProxy;
import io.esastack.httpclient.core.NoopListener;
import io.esastack.httpclient.core.exec.ExecContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.pool.ChannelPool;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class H2TransceiverHandleTest {

    @Test
    void testBuildTimeoutHandle() {
        final H2TransceiverHandle handle = new H2TransceiverHandle();
        final Listener delegate = mock(Listener.class);

        final io.netty.channel.pool.ChannelPool channelPool = mock(ChannelPool.class);
        final Channel channel = mock(Channel.class);

        when(channelPool.release(any(Channel.class))).thenAnswer(answer -> mock(ChannelFuture.class));

        final ListenerProxy proxy = handle.buildTimeoutHandle(channel, channelPool,
                delegate, HttpVersion.HTTP_2);

        final HttpRequest request = mock(HttpRequest.class);
        final Context ctx = mock(Context.class);
        final HttpResponse response = mock(HttpResponse.class);

        final HttpHeaders headers = new Http1HeadersImpl();
        headers.add(io.esastack.commons.net.http.HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        when(response.headers()).thenReturn(headers);

        // release on Error
        proxy.onError(request, ctx, mock(Throwable.class));
        verify(channelPool).release(any());
        verify(delegate).onError(any(), any(), any());
    }

    @Test
    void testAddRspHandle() {
        final H2TransceiverHandle handle = new H2TransceiverHandle();
        final HttpRequest request = mock(HttpRequest.class);
        when(request.headers()).thenReturn(new Http1HeadersImpl());
        final ExecContext ctx = mock(ExecContext.class);

        final HandleRegistry registry = new HandleRegistry(2, 1);
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(new Http1ChannelHandler(registry, -1L));

        then(registry.get(3)).isNull();
        int requestId = handle.addRspHandle(request, ctx, channel,
                null, registry, new TimeoutHandle(NoopListener.INSTANCE), response);
        then(requestId).isEqualTo(3);
        then(registry.get(requestId)).isNotNull();
        then(request.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text())).isEqualTo(requestId);
    }
}
