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
package io.esastack.httpclient.core.config;

import esa.commons.Checks;
import io.esastack.httpclient.core.Reusable;
import io.netty.util.internal.SystemPropertyUtil;

import java.io.Serializable;
import java.util.StringJoiner;

public class CallbackThreadPoolOptions implements Reusable<CallbackThreadPoolOptions>, Serializable {

    private static final String CALLBACK_EXECUTOR_CORE_SIZE =
            "io.esastack.httpclient.callbackExecutor.coreSize";
    private static final String CALLBACK_EXECUTOR_MAX_SIZE =
            "io.esastack.httpclient.callbackExecutor.maxSize";
    private static final String CALLBACK_EXECUTOR_BLOCKING_QUEUE_LENGTH =
            "io.esastack.httpclient.callbackExecutor.blockingQueueLength";
    private static final String CALLBACK_EXECUTOR_KEEP_ALIVE_SECONDS =
            "io.esastack.httpclient.callbackExecutor.keepAliveSeconds";
    private static final String CALLBACK_EXECUTOR_GRACEFULLY_SHUTDOWN_SECONDS =
            "io.esastack.httpclient.callbackExecutor.gracefullyShutdownSeconds";

    private static final long serialVersionUID = -8935787924275761416L;

    private final int coreSize;
    private final int maxSize;
    private final int blockingQueueLength;
    private final long keepAliveSeconds;
    private final long gracefullyShutdownSeconds;

    private CallbackThreadPoolOptions(int coreSize,
                                      int maxSize,
                                      int blockingQueueLength,
                                      long keepAliveSeconds,
                                      long gracefullyShutdownMillis) {
        Checks.checkArg(coreSize >= 0, "coreSize is " + coreSize +
                " (expected >= 0)");
        Checks.checkArg(maxSize > 0, "maxSize is " + maxSize +
                " (expected > 0)");
        Checks.checkArg(blockingQueueLength >= 0, "blockingQueueLength is " + blockingQueueLength +
                " (expected >= 0)");
        Checks.checkArg(keepAliveSeconds >= 0L, "keepAliveSeconds is " + keepAliveSeconds +
                " (expected >= 0L)");
        Checks.checkArg(maxSize >= coreSize, "coreSize is " + coreSize + ", maxSize is "
                + maxSize + " (expected maxSize >= coreSize)");
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.blockingQueueLength = blockingQueueLength;
        this.keepAliveSeconds = keepAliveSeconds;
        this.gracefullyShutdownSeconds = gracefullyShutdownMillis;
    }

    @Override
    public CallbackThreadPoolOptions copy() {
        return new CallbackThreadPoolOptions(coreSize, maxSize, blockingQueueLength,
                keepAliveSeconds, gracefullyShutdownSeconds);
    }

    public static CallbackThreadPoolOptions ofDefault() {
        return new CallbackThreadPoolOptionsBuilder().build();
    }

    public static CallbackThreadPoolOptionsBuilder options() {
        return new CallbackThreadPoolOptionsBuilder();
    }

    public int coreSize() {
        return coreSize;
    }

    public int maxSize() {
        return maxSize;
    }

    public int blockingQueueLength() {
        return blockingQueueLength;
    }

    public long keepAliveSeconds() {
        return keepAliveSeconds;
    }

    public long gracefullyShutdownSeconds() {
        return gracefullyShutdownSeconds;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CallbackThreadPoolOptions.class.getSimpleName() + "[", "]")
                .add("coreSize=" + coreSize)
                .add("maxSize=" + maxSize)
                .add("blockingQueueLength=" + blockingQueueLength)
                .add("keepAliveSeconds=" + keepAliveSeconds)
                .add("gracefullyShutdownSeconds=" + gracefullyShutdownSeconds)
                .toString();
    }

    public static class CallbackThreadPoolOptionsBuilder {

        private static final int DEFAULT_CORE_SIZE = SystemPropertyUtil.getInt(CALLBACK_EXECUTOR_CORE_SIZE, 0);
        private static final int DEFAULT_MAX_SIZE = SystemPropertyUtil.getInt(CALLBACK_EXECUTOR_MAX_SIZE, 0);
        private static final int DEFAULT_BLOCKING_QUEUE_LENGTH =
                SystemPropertyUtil.getInt(CALLBACK_EXECUTOR_BLOCKING_QUEUE_LENGTH, 256);
        private static final long DEFAULT_KEEPALIVE_SECONDS =
                SystemPropertyUtil.getLong(CALLBACK_EXECUTOR_KEEP_ALIVE_SECONDS, 180L);
        private static final long DEFAULT_GRACEFULLY_SHUTDOWN_SECONDS = SystemPropertyUtil.getLong(
                CALLBACK_EXECUTOR_GRACEFULLY_SHUTDOWN_SECONDS, 30L);

        private int coreSize = DEFAULT_CORE_SIZE;
        private int maxSize = DEFAULT_MAX_SIZE;
        private int blockingQueueLength = DEFAULT_BLOCKING_QUEUE_LENGTH;
        private long keepAliveSeconds = DEFAULT_KEEPALIVE_SECONDS;
        private long gracefullyShutdownSeconds = DEFAULT_GRACEFULLY_SHUTDOWN_SECONDS;

        CallbackThreadPoolOptionsBuilder() {
        }

        public CallbackThreadPoolOptionsBuilder coreSize(int coreSize) {
            this.coreSize = coreSize;
            return this;
        }

        public CallbackThreadPoolOptionsBuilder maxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public CallbackThreadPoolOptionsBuilder blockingQueueLength(int blockingQueueLength) {
            this.blockingQueueLength = blockingQueueLength;
            return this;
        }

        public CallbackThreadPoolOptionsBuilder keepAliveSeconds(long keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
            return this;
        }

        public CallbackThreadPoolOptionsBuilder gracefullyShutdownSeconds(long gracefullyShutdownSeconds) {
            this.gracefullyShutdownSeconds = gracefullyShutdownSeconds;
            return this;
        }

        public CallbackThreadPoolOptions build() {
            if (coreSize == 0 && maxSize == 0) {
                return null;
            }
            if (coreSize == 0) {
                coreSize = maxSize;
            } else if (maxSize == 0) {
                maxSize = coreSize;
            }

            return new CallbackThreadPoolOptions(coreSize, maxSize, blockingQueueLength,
                    keepAliveSeconds, gracefullyShutdownSeconds);
        }

    }
}
