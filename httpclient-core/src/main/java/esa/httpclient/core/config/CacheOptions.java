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
import io.netty.util.internal.SystemPropertyUtil;

import java.io.Serializable;
import java.util.StringJoiner;

public class CacheOptions implements Reusable<CacheOptions>, Serializable {

    private static final String INITIAL_CAPACITY_KEY = "esa.httpclient.caching-connectionPools.initialCapacity";
    private static final String MAXIMUM_SIZE_KEY = "esa.httpclient.caching-connectionPools.maximumSize";
    private static final String EXPIRE_SECONDS_KEY = "esa.httpclient.caching-connectionPools.expireAfterAccess";

    private static final long serialVersionUID = -7207026123787537822L;

    private final int initialCapacity;
    private final long maximumSize;
    private final long expireSeconds;

    private CacheOptions(int initialCapacity,
                         long maximumSize,
                         long expireSeconds) {
        Checks.checkArg(initialCapacity >= 0, "initialCapacity is " + initialCapacity +
                " (expected >= 0)");
        Checks.checkArg(maximumSize >= 0L, "maximumSize is " + maximumSize +
                " (expected >= 0L)");
        Checks.checkArg(expireSeconds >= 0L, "expireSeconds is " + expireSeconds +
                " (expected >= 0L)");
        this.initialCapacity = initialCapacity;
        this.maximumSize = maximumSize;
        this.expireSeconds = expireSeconds;
    }

    public static CacheOptions ofDefault() {
        return new CacheOptionsBuilder().build();
    }

    public static CacheOptionsBuilder options() {
        return new CacheOptionsBuilder();
    }

    public int initialCapacity() {
        return initialCapacity;
    }

    public long maximumSize() {
        return maximumSize;
    }

    public long expireSeconds() {
        return expireSeconds;
    }

    @Override
    public CacheOptions copy() {
        return new CacheOptions(initialCapacity, maximumSize, expireSeconds);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CacheOptions.class.getSimpleName() + "[", "]")
                .add("initialCapacity=" + initialCapacity)
                .add("maximumSize=" + maximumSize)
                .add("expireSeconds=" + expireSeconds)
                .toString();
    }

    public static class CacheOptionsBuilder {

        private static final int DEFAULT_INITIAL_CAPACITY = SystemPropertyUtil.getInt(INITIAL_CAPACITY_KEY, 16);
        private static final long DEFAULT_MAXIMUM_SIZE = SystemPropertyUtil.getLong(MAXIMUM_SIZE_KEY, 500L);
        private static final long DEFAULT_EXPIRE_SECONDS = SystemPropertyUtil.getLong(EXPIRE_SECONDS_KEY, 600L);

        private int initialCapacity = DEFAULT_INITIAL_CAPACITY;
        private long maximumSize = DEFAULT_MAXIMUM_SIZE;
        private long expireSeconds = DEFAULT_EXPIRE_SECONDS;

        CacheOptionsBuilder() {
        }

        public CacheOptionsBuilder initialCapacity(int initialCapacity) {
            this.initialCapacity = initialCapacity;
            return this;
        }

        public CacheOptionsBuilder maximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
            return this;
        }

        public CacheOptionsBuilder expireSeconds(long expireSeconds) {
            this.expireSeconds = expireSeconds;
            return this;
        }

        public CacheOptions build() {
            return new CacheOptions(initialCapacity, maximumSize, expireSeconds);
        }

    }

}
