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

import esa.commons.Checks;
import esa.commons.concurrent.NettyInternalThread;
import io.esastack.httpclient.core.util.LoggerUtils;
import io.netty.util.concurrent.FastThreadLocalThread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is a thread factory which use {@link FastThreadLocalThread} as generated thread. Be aware that, this factory
 * is designed as package visible and there is no perception of users when we change.
 */
class ThreadFactoryImpl implements ThreadFactory {

    private static final AtomicInteger POOL_ID = new AtomicInteger();

    private final AtomicInteger nextId = new AtomicInteger();
    private final String prefix;
    private final boolean daemon;

    ThreadFactoryImpl(String prefix, boolean daemon) {
        Checks.checkNotNull(prefix, "prefix");
        this.prefix = prefix + "-" + POOL_ID.getAndIncrement() + "#";
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = prefix + nextId.getAndIncrement();
        Thread t = new InternalThread(r, prefix, name);
        try {
            if (t.isDaemon() != daemon) {
                t.setDaemon(daemon);
            }
        } catch (Exception ignored) {
            // Doesn't matter even if failed to set.
        }
        t.setUncaughtExceptionHandler((thread, error) ->
                LoggerUtils.logger().error("Caught unexpected exception in thread: "
                        + thread.getName(), error)
        );
        return t;
    }

    private static class InternalThread extends NettyInternalThread {

        private InternalThread(Runnable target, String prefix, String name) {
            super(target, name);
            LoggerUtils.logger().info("Creating " + prefix + " thread: {}", name);
        }

    }
}
