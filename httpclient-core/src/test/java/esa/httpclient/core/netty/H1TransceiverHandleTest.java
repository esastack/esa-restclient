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

import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.httpclient.core.Context;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.ListenerProxy;
import esa.httpclient.core.NoopListener;
import esa.httpclient.core.exec.ExecContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.pool.ChannelPool;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class H1TransceiverHandleTest {

    @Test
    void testBuildTimeoutHandle() {
        final H1TransceiverHandle handle = new H1TransceiverHandle();
        final Listener delegate = mock(Listener.class);

        final ChannelPool channelPool = mock(ChannelPool.class);
        final Channel channel = mock(Channel.class);

        when(channelPool.release(any(Channel.class))).thenAnswer(answer -> mock(ChannelFuture.class));
        when(channel.close()).thenAnswer(answer -> mock(ChannelFuture.class));

        final ListenerProxy proxy = handle.buildTimeoutHandle(channel, channelPool,
                delegate, HttpVersion.HTTP_1_1);

        final HttpRequest request = mock(HttpRequest.class);
        final Context ctx = mock(Context.class);
        final HttpResponse response = mock(HttpResponse.class);

        final HttpHeaders headers = new Http1HeadersImpl();
        headers.add(esa.commons.http.HttpHeaderNames.CONNECTION, "Close");
        when(response.headers()).thenReturn(headers);

        // Case 1: close if necessary and release onCompleted.
        proxy.onCompleted(request, ctx, response);
        verify(channel).close();
        verify(channelPool).release(any(Channel.class));
        verify(delegate).onCompleted(any(), any(), any());

        // Case 2: release on Error
        proxy.onError(request, ctx, mock(Throwable.class));
        verify(channelPool, times(1)).release(any(Channel.class));
        verify(delegate).onError(any(), any(), any());

        // Case 3: release for http1.0
        clearInvocations(delegate);
        clearInvocations(channel);
        clearInvocations(channelPool);

        final ListenerProxy proxy10 = handle.buildTimeoutHandle(channel, channelPool,
                delegate, HttpVersion.HTTP_1_0);
        headers.clear();

        proxy10.onCompleted(request, ctx, response);
        verify(channel, times(1)).close();
        verify(channelPool, times(1)).release(any(Channel.class));
        verify(delegate).onCompleted(any(), any(), any());
    }

    @Test
    void testAddRspHandle() {
        final H1TransceiverHandle handle = new H1TransceiverHandle();
        final HttpRequest request = mock(HttpRequest.class);
        final ExecContext ctx = mock(ExecContext.class);
        final HandleRegistry registry = new HandleRegistry(1, 0);
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();

        final EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(new Http1ChannelHandler(registry, -1L));

        int requestId = handle.addRspHandle(request, ctx, channel,
                null, registry, new TimeoutHandle(NoopListener.INSTANCE), response);
        then(requestId).isEqualTo(1);
        then(registry.get(requestId)).isNotNull();
    }
}
