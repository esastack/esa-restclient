/*
 * Copyright 2021 OPPO ESA Stack Project
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
package esa.httpclient.core.exec;

import esa.commons.Checks;
import esa.httpclient.core.Context;
import esa.httpclient.core.Handle;
import esa.httpclient.core.Handler;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.Listener;
import esa.httpclient.core.netty.HandleImpl;
import esa.httpclient.core.netty.NettyResponse;
import esa.httpclient.core.util.LoggerUtils;

import java.util.function.Consumer;

public class ExecContext {

    private final Context ctx;
    private final Listener listener;
    private final Consumer<Handle> handle;
    private final Handler handler;

    private volatile Runnable continueCallback;

    public ExecContext(Context ctx, Listener listener, Consumer<Handle> handle, Handler handler) {
        Checks.checkNotNull(ctx, "ctx");
        Checks.checkNotNull(listener, "listener");
        this.ctx = ctx;
        this.listener = listener;
        this.handle = handle;
        this.handler = handler;
    }

    public Context ctx() {
        return ctx;
    }

    public Listener listener() {
        return listener;
    }

    public void set100ContinueCallback(Runnable callback) {
        this.continueCallback = callback;
    }

    public Runnable remove100ContinueCallback() {
        final Runnable callback0 = continueCallback;
        this.continueCallback = null;
        return callback0;
    }

    public HandleImpl handleImpl(HttpRequest request) {
        if (handler != null && handle != null) {
            LoggerUtils.logger().warn("Both handler and consumer<handle> are found to handle the" +
                    "inbound message, the handler will be used, uri: {}", request.uri());
        }
        if (handler != null) {
            return new HandleImpl(new NettyResponse(false), handler);
        } else if (handle != null) {
            return new HandleImpl(new NettyResponse(false), handle);
        }

        LoggerUtils.logger().debug("The default handle will be used to aggregate the inbound message to" +
                " a response");

        return null;
    }
}

