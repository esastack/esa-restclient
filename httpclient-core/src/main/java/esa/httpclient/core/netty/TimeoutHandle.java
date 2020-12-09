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
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.ListenerProxy;
import io.netty.util.Timeout;

/**
 * The class is designed as thread-safe, because the {@code task} will only
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
