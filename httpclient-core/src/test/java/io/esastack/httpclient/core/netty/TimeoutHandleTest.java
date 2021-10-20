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

import io.esastack.httpclient.core.Listener;
import io.netty.util.Timeout;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TimeoutHandleTest {

    @Test
    void testOnCompleted() {
        final Listener listener = mock(Listener.class);
        final TimeoutHandle handle = new TimeoutHandle(listener);
        handle.onCompleted(null, null, null);
        verify(listener).onCompleted(null, null, null);

        clearInvocations(listener);
        final Timeout timeout = mock(Timeout.class);
        handle.addCancelTask(timeout);
        handle.onCompleted(null, null, null);
        verify(timeout).cancel();
        verify(listener).onCompleted(null, null, null);

        clearInvocations(timeout);
        handle.onError(null, null, null);
        verify(timeout, never()).cancel();
    }

    @Test
    void testOnError() {
        final Listener listener = mock(Listener.class);
        final TimeoutHandle handle = new TimeoutHandle(listener);
        handle.onError(null, null, null);
        verify(listener).onError(null, null, null);

        clearInvocations(listener);
        final Timeout timeout = mock(Timeout.class);
        handle.addCancelTask(timeout);
        handle.onError(null, null, null);
        verify(timeout).cancel();
        verify(listener).onError(null, null, null);

        clearInvocations(timeout);
        handle.onCompleted(null, null, null);
        verify(timeout, never()).cancel();
    }
}
