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
package io.esastack.httpclient.core;

import esa.commons.annotation.Internal;
import io.esastack.httpclient.core.exec.ExecContext;
import io.esastack.httpclient.core.netty.NettyExecContext;

/**
 * The helper utility for test purpose.
 */
@Internal
public class ExecContextUtil {

    public static ExecContext newAs() {
        return new ExecContext(new Context(), NoopListener.INSTANCE, null, null);
    }

    public static NettyExecContext newAsNetty() {
        return new NettyExecContext(new Context(), NoopListener.INSTANCE, null, null);
    }

    public static ExecContext from(Context ctx) {
        return new ExecContext(ctx, NoopListener.INSTANCE, null, null);
    }

    public static NettyExecContext from(Context ctx, Listener listener) {
        return new NettyExecContext(ctx, listener, null, null);
    }
}

