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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
        assertThrows(Exception.class, () -> proxy.onInterceptorsStart(request, ctx));
        verify(normal, never()).onInterceptorsStart(request, ctx);

        doThrow(ex).when(abnormal).onInterceptorsEnd(request, ctx);
        assertThrows(Exception.class, () -> proxy.onInterceptorsEnd(request, ctx));
        verify(normal, never()).onInterceptorsEnd(request, ctx);

        doThrow(ex).when(abnormal).onFiltersStart(request, fCtx);
        assertThrows(Exception.class, () -> proxy.onFiltersStart(request, fCtx));
        verify(normal, never()).onFiltersStart(request, fCtx);

        doThrow(ex).when(abnormal).onFiltersEnd(request, ctx);
        assertThrows(Exception.class, () -> proxy.onFiltersEnd(request, ctx));
        verify(normal, never()).onFiltersEnd(request, ctx);

        doThrow(ex).when(abnormal).onConnectionPoolAttempt(request, ctx, address);
        assertThrows(Exception.class, () -> proxy.onConnectionPoolAttempt(request, ctx, address));
        verify(normal, never()).onConnectionPoolAttempt(request, ctx, address);

        doThrow(ex).when(abnormal).onConnectionPoolAcquired(request, ctx, address);
        assertThrows(Exception.class, () -> proxy.onConnectionPoolAcquired(request, ctx, address));
        verify(normal, never()).onConnectionPoolAcquired(request, ctx, address);

        doThrow(ex).when(abnormal).onAcquireConnectionPoolFailed(request, ctx, address, cause);
        assertThrows(Exception.class, () -> proxy.onAcquireConnectionPoolFailed(request, ctx, address, cause));
        verify(normal, never()).onAcquireConnectionPoolFailed(request, ctx, address, cause);

        doThrow(ex).when(abnormal).onConnectionAttempt(request, ctx, address);
        assertThrows(Exception.class, () -> proxy.onConnectionAttempt(request, ctx, address));
        verify(normal, never()).onConnectionAttempt(request, ctx, address);

        doThrow(ex).when(abnormal).onConnectionAcquired(request, ctx, address);
        assertThrows(Exception.class, () -> proxy.onConnectionAcquired(request, ctx, address));
        verify(normal, never()).onConnectionAcquired(request, ctx, address);

        doThrow(ex).when(abnormal).onAcquireConnectionFailed(request, ctx, address, cause);
        assertThrows(Exception.class, () -> proxy.onAcquireConnectionFailed(request, ctx, address, cause));
        verify(normal, never()).onAcquireConnectionFailed(request, ctx, address, cause);

        doThrow(ex).when(abnormal).onWriteAttempt(request, ctx, -1L);
        assertThrows(Exception.class, () -> proxy.onWriteAttempt(request, ctx, -1L));
        verify(normal, never()).onWriteAttempt(request, ctx, -1L);

        doThrow(ex).when(abnormal).onWriteDone(request, ctx, -1L);
        assertThrows(Exception.class, () -> proxy.onWriteDone(request, ctx, -1L));
        verify(normal, never()).onWriteDone(request, ctx, -1L);

        doThrow(ex).when(abnormal).onWriteFailed(request, ctx, cause);
        assertThrows(Exception.class, () -> proxy.onWriteFailed(request, ctx, cause));
        verify(normal, never()).onWriteFailed(request, ctx, cause);

        doThrow(ex).when(abnormal).onMessageReceived(request, ctx, message);
        assertThrows(Exception.class, () -> proxy.onMessageReceived(request, ctx, message));
        verify(normal, never()).onMessageReceived(request, ctx, message);

        doThrow(ex).when(abnormal).onCompleted(request, ctx, response);
        assertThrows(Exception.class, () -> proxy.onCompleted(request, ctx, response));
        verify(normal, never()).onCompleted(request, ctx, response);

        doThrow(ex).when(abnormal).onError(request, ctx, cause);
        assertThrows(Exception.class, () -> proxy.onError(request, ctx, cause));
        verify(normal, never()).onError(request, ctx, cause);
    }
}
