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
package esa.httpclient.core.config;

import esa.commons.Checks;
import esa.httpclient.core.Reusable;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class ChannelPoolOptions implements Reusable<ChannelPoolOptions>, Serializable {

    private static final long serialVersionUID = 802132190143196865L;

    private final long readTimeout;
    private final int connectTimeout;
    private final int poolSize;
    private final int waitingQueueLength;

    private ChannelPoolOptions(long readTimeout, int connectTimeout, int poolSize, int waitingQueueLength) {
        Checks.checkArg(readTimeout >= 1, "readTimeout is " + readTimeout +
                " (expected >= 1)");
        Checks.checkArg(connectTimeout >= 1, "connectTimeout is " + connectTimeout +
                " (expected >= 1)");
        Checks.checkArg(poolSize >= 1, "poolSize is " + poolSize +
                " (expected >= 1)");
        Checks.checkArg(waitingQueueLength >= 1, "waitingQueueLength is " + waitingQueueLength +
                " (expected >= 1)");
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
        this.poolSize = poolSize;
        this.waitingQueueLength = waitingQueueLength;
    }

    @Override
    public ChannelPoolOptions copy() {
        return new ChannelPoolOptions(readTimeout, connectTimeout, poolSize, waitingQueueLength);
    }

    public static ChannelPoolOptions ofDefault() {
        return new ChannelPoolOptionsBuilder().build();
    }

    public static ChannelPoolOptionsBuilder options() {
        return new ChannelPoolOptionsBuilder();
    }

    public long readTimeout() {
        return readTimeout;
    }

    public int connectTimeout() {
        return connectTimeout;
    }

    public int poolSize() {
        return poolSize;
    }

    public int waitingQueueLength() {
        return waitingQueueLength;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ChannelPoolOptions.class.getSimpleName() + "[", "]")
                .add("readTimeout=" + readTimeout)
                .add("connectTimeout=" + connectTimeout)
                .add("poolSize=" + poolSize)
                .add("waitingQueueLength=" + waitingQueueLength)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelPoolOptions options = (ChannelPoolOptions) o;
        return readTimeout == options.readTimeout &&
                connectTimeout == options.connectTimeout &&
                poolSize == options.poolSize &&
                waitingQueueLength == options.waitingQueueLength;
    }

    @Override
    public int hashCode() {
        return Objects.hash(readTimeout, connectTimeout, poolSize, waitingQueueLength);
    }

    public static class ChannelPoolOptionsBuilder {

        private long readTimeout = 6000;
        private int connectTimeout = 3000;
        private int poolSize = 512;
        private int waitingQueueLength = 256;

        ChannelPoolOptionsBuilder() {
        }

        public ChannelPoolOptionsBuilder readTimeout(long readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public ChannelPoolOptionsBuilder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public ChannelPoolOptionsBuilder poolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        public ChannelPoolOptionsBuilder waitingQueueLength(int waitingQueueLength) {
            this.waitingQueueLength = waitingQueueLength;
            return this;
        }

        public ChannelPoolOptions build() {
            return new ChannelPoolOptions(readTimeout, connectTimeout, poolSize, waitingQueueLength);
        }

    }
}

