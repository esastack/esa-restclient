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

import io.esastack.httpclient.core.IdentityFactory;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

class IdentityFactoryProvider {

    private static final IdentityFactory<EventLoopGroup> IO_THREADS_IDENTITY_FACTORY =
            new IdentityFactory<EventLoopGroup>() {

                private static final String PREFIX = "IO-Threads-Pool-";
                private final AtomicInteger index = new AtomicInteger();

                @Override
                public Identified<EventLoopGroup> generate(EventLoopGroup value) {
                    return new Identified<>(value, PREFIX + index.incrementAndGet());
                }
            };

    private static final IdentityFactory<ThreadPoolExecutor> CALLBACK_EXECUTOR_IDENTITY_FACTORY =
            new IdentityFactory<ThreadPoolExecutor>() {

                private static final String PREFIX = "Callback-Executor-";
                private final AtomicInteger index = new AtomicInteger();

                @Override
                public Identified<ThreadPoolExecutor> generate(ThreadPoolExecutor value) {
                    return new Identified<>(value, PREFIX + index.incrementAndGet());
                }
            };

    private IdentityFactoryProvider() {
    }

    static IdentityFactory<EventLoopGroup> ioThreadsIdentityFactory() {
        return IO_THREADS_IDENTITY_FACTORY;
    }

    static IdentityFactory<ThreadPoolExecutor> callbackExecutorIdentityFactory() {
        return CALLBACK_EXECUTOR_IDENTITY_FACTORY;
    }

}
