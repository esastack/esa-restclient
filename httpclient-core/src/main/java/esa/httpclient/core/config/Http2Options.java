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
import io.netty.handler.codec.http2.Http2CodecUtil;

import java.io.Serializable;
import java.util.StringJoiner;

public class Http2Options implements Reusable<Http2Options>, Serializable {

    private static final long serialVersionUID = -2062493018634619158L;

    private final int maxReservedStreams;
    private final int maxFrameSize;
    private final long gracefulShutdownTimeoutMillis;

    private Http2Options(int maxReservedStreams,
                         int maxFrameSize,
                         long gracefulShutdownTimeoutMillis) {
        Checks.checkArg(maxReservedStreams >= 0, "maxReservedStreams is " + maxReservedStreams +
                " (expected >= 0)");
        Checks.checkArg(Http2CodecUtil.isMaxFrameSizeValid(maxFrameSize), "maxFrameSize is invalid");
        Checks.checkArg(gracefulShutdownTimeoutMillis >= -1L, "gracefulShutdownTimeoutMillis is "
                + gracefulShutdownTimeoutMillis + " (expected > -1L)");
        this.maxReservedStreams = maxReservedStreams;
        this.maxFrameSize = maxFrameSize;
        this.gracefulShutdownTimeoutMillis = gracefulShutdownTimeoutMillis;
    }

    public static Http2Options ofDefault() {
        return new Http2OptionsBuilder().build();
    }

    public static Http2OptionsBuilder options() {
        return new Http2OptionsBuilder();
    }

    public int maxReservedStreams() {
        return maxReservedStreams;
    }

    public int maxFrameSize() {
        return maxFrameSize;
    }

    public long gracefulShutdownTimeoutMillis() {
        return gracefulShutdownTimeoutMillis;
    }

    @Override
    public Http2Options copy() {
        return new Http2Options(maxReservedStreams, maxFrameSize, gracefulShutdownTimeoutMillis);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Http2Options.class.getSimpleName() + "[", "]")
                .add("maxReservedStreams=" + maxReservedStreams)
                .add("maxFrameSize=" + maxFrameSize)
                .add("gracefulShutdownTimeoutMillis=" + gracefulShutdownTimeoutMillis)
                .toString();
    }

    public static class Http2OptionsBuilder {

        private int maxReservedStreams = Http2CodecUtil.SMALLEST_MAX_CONCURRENT_STREAMS;
        private int maxFrameSize = Http2CodecUtil.DEFAULT_MAX_FRAME_SIZE;

        /**
         * @see io.netty.handler.codec.http2.Http2CodecUtil#DEFAULT_GRACEFUL_SHUTDOWN_TIMEOUT_MILLIS
         */
        private long gracefulShutdownTimeoutMillis = 30_000L;

        Http2OptionsBuilder() {
        }

        public Http2OptionsBuilder maxReservedStreams(int maxReservedStreams) {
            this.maxReservedStreams = maxReservedStreams;
            return this;
        }

        public Http2OptionsBuilder maxFrameSize(int maxFrameSize) {
            this.maxFrameSize = maxFrameSize;
            return this;
        }

        public Http2OptionsBuilder gracefulShutdownTimeoutMillis(long gracefulShutdownTimeoutMillis) {
            this.gracefulShutdownTimeoutMillis = gracefulShutdownTimeoutMillis;
            return this;
        }

        public Http2Options build() {
            return new Http2Options(maxReservedStreams, maxFrameSize, gracefulShutdownTimeoutMillis);
        }
    }
}
