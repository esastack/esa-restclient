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
import io.esastack.httpclient.core.ListenerProxy;
import io.netty.util.Timeout;

/**
 * This class is designed as thread-safe, because the instance will only
 * be accessed by a fixed IO-Thread.
 */
class TimeoutHandle extends ListenerProxy {

    private Timeout task;

    TimeoutHandle(Listener delegate) {
        super(delegate);
    }

    void addCancelTask(Timeout task) {
        this.task = task;
    }

    @Override
    public void onCompleted(HttpRequest request, Context ctx, HttpResponse response) {
        super.onCompleted(request, ctx, response);

        // Note: Cancel the read timeout task immediately when the request has completed,
        // so that the GC can reclaim the task as soon as possible. See more details from
        // Timeout.cancel().
        cancelAndClean();
    }

    @Override
    public void onError(HttpRequest request, Context ctx, Throwable cause) {
        super.onError(request, ctx, cause);
        cancelAndClean();
    }

    private void cancelAndClean() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
