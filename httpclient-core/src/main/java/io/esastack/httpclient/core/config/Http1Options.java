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

import java.io.Serializable;
import java.util.StringJoiner;

public class Http1Options implements Reusable<Http1Options>, Serializable {

    private static final long serialVersionUID = 84321473660029244L;

    private final int maxInitialLineLength;
    private final int maxHeaderSize;
    private final int maxChunkSize;

    private Http1Options(int maxInitialLineLength,
                         int maxHeaderSize,
                         int maxChunkSize) {
        Checks.checkArg(maxInitialLineLength > 0, "maxInitialLineLength is " +
                maxInitialLineLength + " (expected > 0)");
        Checks.checkArg(maxHeaderSize > 0, "maxHeaderSize is " + maxHeaderSize +
                " (expected > 0)");
        Checks.checkArg(maxChunkSize > 0, "maxChunkSize is " + maxChunkSize +
                " (expected > 0)");
        this.maxInitialLineLength = maxInitialLineLength;
        this.maxHeaderSize = maxHeaderSize;
        this.maxChunkSize = maxChunkSize;
    }

    @Override
    public Http1Options copy() {
        return new Http1Options(maxInitialLineLength, maxHeaderSize, maxChunkSize);
    }

    public static Http1Options ofDefault() {
        return new Http1OptionsBuilder().build();
    }

    public static Http1OptionsBuilder options() {
        return new Http1OptionsBuilder();
    }

    public int maxInitialLineLength() {
        return maxInitialLineLength;
    }

    public int maxHeaderSize() {
        return maxHeaderSize;
    }

    public int maxChunkSize() {
        return maxChunkSize;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Http1Options.class.getSimpleName() + "[", "]")
                .add("maxInitialLineLength=" + maxInitialLineLength)
                .add("maxHeaderSize=" + maxHeaderSize)
                .add("maxChunkSize=" + maxChunkSize)
                .toString();
    }

    public static class Http1OptionsBuilder {
        private int maxInitialLineLength = 4096;
        private int maxHeaderSize = 8192;
        private int maxChunkSize = 8192;

        Http1OptionsBuilder() {
        }

        public Http1OptionsBuilder maxInitialLineLength(int maxInitialLineLength) {
            this.maxInitialLineLength = maxInitialLineLength;
            return this;
        }

        public Http1OptionsBuilder maxHeaderSize(int maxHeaderSize) {
            this.maxHeaderSize = maxHeaderSize;
            return this;
        }

        public Http1OptionsBuilder maxChunkSize(int maxChunkSize) {
            this.maxChunkSize = maxChunkSize;
            return this;
        }

        public Http1Options build() {
            return new Http1Options(maxInitialLineLength, maxHeaderSize, maxChunkSize);
        }
    }

}
