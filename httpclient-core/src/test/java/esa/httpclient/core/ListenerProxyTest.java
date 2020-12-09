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
package esa.httpclient.core;

import esa.httpclient.core.filter.FilterContext;
import org.junit.jupiter.api.Test;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListenerProxyTest {

    @Test
    void testConstructor() {
        then(ListenerProxy.DEFAULT).isSameAs(NoopListener.INSTANCE);

        new ListenerProxy((Listener) null);
        new ListenerProxy((List<Listener>) null);
        new ListenerProxy(mock(Listener.class));
    }

    @Test
    void testBasic() {
        final List<Listener> delegates = new ArrayList<>();

        final Listener abnormal = mock(Listener.class);
        final Listener normal = mock(Listener.class);
        delegates.add(abnormal);
        delegates.add(normal);

        final ListenerProxy proxy = new ListenerProxy(delegates);
        final HttpRequest request = mock(HttpRequest.class);
        final Context ctx = mock(Context.class);
        final FilterContext fCtx = mock(FilterContext.class);
        final SocketAddress address = mock(SocketAddress.class);
        final Throwable cause = mock(Throwable.class);
        final HttpMessage message = mock(HttpMessage.class);
        final HttpResponse response = mock(HttpResponse.class);

        when(request.uri()).thenReturn(new HttpUri("http://127.0.0.1:8080"));
        final Exception ex = new RuntimeException("Listener error");

        doThrow(ex).when(abnormal).onInterceptorsStart(request, ctx);
        proxy.onInterceptorsStart(request, ctx);
        verify(normal).onInterceptorsStart(request, ctx);

        doThrow(ex).when(abnormal).onInterceptorsEnd(request, ctx);
        proxy.onInterceptorsEnd(request, ctx);
        verify(normal).onInterceptorsEnd(request, ctx);

        doThrow(ex).when(abnormal).onFiltersStart(request, fCtx);
        proxy.onFiltersStart(request, fCtx);
        verify(normal).onFiltersStart(request, fCtx);

        doThrow(ex).when(abnormal).onFiltersEnd(request, ctx);
        proxy.onFiltersEnd(request, ctx);
        verify(normal).onFiltersEnd(request, ctx);

        doThrow(ex).when(abnormal).onConnectionPoolAttempt(request, ctx, address);
        proxy.onConnectionPoolAttempt(request, ctx, address);
        verify(normal).onConnectionPoolAttempt(request, ctx, address);

        doThrow(ex).when(abnormal).onConnectionPoolAcquired(request, ctx, address);
        proxy.onConnectionPoolAcquired(request, ctx, address);
        verify(normal).onConnectionPoolAcquired(request, ctx, address);

        doThrow(ex).when(abnormal).onAcquireConnectionPoolFailed(request, ctx, address, cause);
        proxy.onAcquireConnectionPoolFailed(request, ctx, address, cause);
        verify(normal).onAcquireConnectionPoolFailed(request, ctx, address, cause);

        doThrow(ex).when(abnormal).onConnectionAttempt(request, ctx, address);
        proxy.onConnectionAttempt(request, ctx, address);
        verify(normal).onConnectionAttempt(request, ctx, address);

        doThrow(ex).when(abnormal).onConnectionAcquired(request, ctx, address);
        proxy.onConnectionAcquired(request, ctx, address);
        verify(normal).onConnectionAcquired(request, ctx, address);

        doThrow(ex).when(abnormal).onAcquireConnectionFailed(request, ctx, address, cause);
        proxy.onAcquireConnectionFailed(request, ctx, address, cause);
        verify(normal).onAcquireConnectionFailed(request, ctx, address, cause);

        doThrow(ex).when(abnormal).onWriteAttempt(request, ctx, -1L);
        proxy.onWriteAttempt(request, ctx, -1L);
        verify(normal).onWriteAttempt(request, ctx, -1L);

        doThrow(ex).when(abnormal).onWriteDone(request, ctx, -1L);
        proxy.onWriteDone(request, ctx, -1L);
        verify(normal).onWriteDone(request, ctx, -1L);

        doThrow(ex).when(abnormal).onWriteFailed(request, ctx, cause);
        proxy.onWriteFailed(request, ctx, cause);
        verify(normal).onWriteFailed(request, ctx, cause);

        doThrow(ex).when(abnormal).onMessageReceived(request, ctx, message);
        proxy.onMessageReceived(request, ctx, message);
        verify(normal).onMessageReceived(request, ctx, message);

        doThrow(ex).when(abnormal).onCompleted(request, ctx, response);
        proxy.onCompleted(request, ctx, response);
        verify(normal).onCompleted(request, ctx, response);

        doThrow(ex).when(abnormal).onError(request, ctx, cause);
        proxy.onError(request, ctx, cause);
        verify(normal).onError(request, ctx, cause);
    }
}
